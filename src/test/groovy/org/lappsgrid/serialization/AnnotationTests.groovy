package org.lappsgrid.serialization

import org.junit.*
import org.lappsgrid.discriminator.Discriminators
import org.lappsgrid.serialization.lif.Annotation

import static org.junit.Assert.*

/**
 * @author Keith Suderman
 */
class AnnotationTests {

    static final String FEATURE_NAME = "feature"
    static final String FEATURE_VALUE = "value"

    Annotation annotation

    @Before
    void setup() {
        annotation = new Annotation("id", "type", 0, 1)
    }

    @After
    void teardown() {
        annotation = null
    }

    @Test
    void testCopyConstructor() {
        annotation.features.pos = 'VB'
        annotation.features.word = 'be'
        annotation.features.list = ['a', 'b', 'c']
        annotation.features.set = ['d', 'e', 'f'] as HashSet
        Annotation copy = new Annotation(annotation)
        // This ensures a deep copy was performed of original.
        annotation.features = [:]
        assertEquals('id', copy.id)
        assertEquals('type', copy.atType)
        assertEquals(0, copy.start)
        assertEquals(1, copy.end)
        assertEquals(4, copy.features.size())
        assertEquals('VB', copy.features.pos)
        assertEquals('be', copy.features.word)
        assertTrue copy.features.list == ['a', 'b', 'c']
        assertTrue copy.features.set == ['d', 'e', 'f'] as HashSet
    }

    @Test
    void testAddSetFeature() {
        // Round-trip the annotation through a JSON string.
        annotation.addFeature("set", [1,2,3] as HashSet)
        String json = Serializer.toJson(annotation)
        annotation = Serializer.parse(json, Annotation)
        assertTrue(annotation.features.set == [1,2,3])
    }

    @Test
    void testGetFeatureSet() {
        annotation.addFeature(FEATURE_NAME, [1, 2, 3] as HashSet)
        Object value = annotation.getFeatureSet(FEATURE_NAME)
        assertTrue (value instanceof Set)
        Set set = (Set) value
        assertTrue set == [1,2,3] as HashSet
    }

    @Test
    void testFeatureFeatureListAsSet() {
        annotation.addFeature(FEATURE_NAME, [1,2,3])
        Object value = annotation.getFeatureSet(FEATURE_NAME)
        assertTrue (value instanceof Set)
        Set set = (Set) value
        assertEquals(3, set.size())
        assertTrue(set.contains(1))
        assertTrue(set.contains(2))
        assertTrue(set.contains(3))
    }

    @Test(expected = IllegalArgumentException)
    void testGetFeatureSetEx() {
        annotation.addFeature(FEATURE_NAME, 'value')
        annotation.getFeatureSet(FEATURE_NAME)
    }

    @Test
    void testGetFeatureMap() {
        annotation.addFeature(FEATURE_NAME, [key: 'value'])
        Map map = annotation.getFeatureMap(FEATURE_NAME)
        assertNotNull(map)
        assertEquals(1, map.size())
        assertEquals('value', map.key)
    }

    @Test(expected = IllegalArgumentException)
    void testGetFeatureMapEx() {
        annotation.addFeature(FEATURE_NAME, 'value')
        annotation.getFeatureMap(FEATURE_NAME)
    }

    @Test
    void testGetFeatureList() {
        annotation.addFeature(FEATURE_NAME, [1,2,3])
        Object value = annotation.getFeatureList(FEATURE_NAME)
        assertTrue(value instanceof List)
        List list = (List) value
        assertEquals(3, list.size())
        assertEquals(1, list[0])
        assertEquals(2, list[1])
        assertEquals(3, list[2])
    }

    @Test(expected = IllegalArgumentException)
    void testGetFeatureListEx() {
        annotation.addFeature(FEATURE_NAME, 'value')
        annotation.getFeatureList(FEATURE_NAME)
    }

    @Test
    void testEmptyFeatureList() {
        List empty = annotation.getFeatureList(FEATURE_NAME)
        assertNotNull empty
        assertEquals 0, empty.size()
    }

    @Test
    void testModifyEmptyFeatureList() {
        List list = annotation.getFeatureList(FEATURE_NAME)
        assertEquals(0, list.size())
        list.add(FEATURE_VALUE)
        list = annotation.getFeatureList(FEATURE_NAME)
        assertEquals(1, list.size())
        assertEquals(FEATURE_VALUE, list[0])
    }

    @Test
    void testEmptyFeatureMap() {
        Map empty = annotation.getFeatureMap(FEATURE_NAME)
        assertNotNull empty
        assertEquals(0, empty.size())
    }

    @Test
    void testModifyEmptyFeatureMap() {
        Map map = annotation.getFeatureMap(FEATURE_NAME)
        map[FEATURE_NAME] = FEATURE_VALUE
        map = annotation.getFeatureMap(FEATURE_NAME)
        assertEquals(1, map.size())
        assertEquals(FEATURE_VALUE, map[FEATURE_NAME])
    }

    @Test
    void testEmptyFeatureSet() {
        Set empty = annotation.getFeatureSet(FEATURE_NAME)
        assertNotNull empty
        assertEquals(0, empty.size())
    }

    @Test
    void testModifyEmptyFeatureSet() {
        Set set = annotation.getFeatureSet(FEATURE_NAME)
        // Add a duplicate value to ensure we really have a set.
        set.add(FEATURE_VALUE)
        set.add(FEATURE_VALUE)
        set.add(FEATURE_NAME)
        assertEquals(2, set.size())
        set = annotation.getFeatureSet(FEATURE_NAME)
        assertEquals(2, set.size())
    }

    @Test
    void testConstructors() {
        Annotation a = new Annotation(Discriminators.Uri.TOKEN, 0, 1)
        assertNull a.id
        assertEquals Discriminators.Uri.TOKEN, a.atType
        assertNull a.label
        assertEquals 0, a.start
        assertEquals 1, a.end
        assertNotNull a.features
        assertEquals 0, a.features.size()

        a = new Annotation('a0', Discriminators.Uri.TOKEN, 0, 1)
        assertEquals 'a0', a.id
        assertEquals Discriminators.Uri.TOKEN, a.atType
        assertNull a.label
        assertEquals 0, a.start
        assertEquals 1, a.end
        assertNotNull a.features
        assertEquals 0, a.features.size()

        a = new Annotation('a0', Discriminators.Uri.TOKEN, "The Label", 0, 1)
        assertEquals 'a0', a.id
        assertEquals Discriminators.Uri.TOKEN, a.atType
        assertEquals "The Label", a.label
        assertEquals 0, a.start
        assertEquals 1, a.end
        assertNotNull a.features
        assertEquals 0, a.features.size()
    }

    @Test
    void testAtTypeFromMapConstructor() {
        String label = "The Label"
        //Annotation a = new Annotation("a1", Discriminators.Uri.TOKEN, label, 0, 5)
        //Map map = Serializer.parse(Serializer.toJson(a), HashMap.class)
        Map map = [
                id:'a1',
                '@type': Discriminators.Uri.TOKEN,
                label:label,
                features: [
                        word:'hello',
                        pos: 'NN'
                ],
                start:0,
                end:5
        ]
        Annotation a = new Annotation(map)
        assertEquals 'a1', a.id
        assertEquals Discriminators.Uri.TOKEN, a.atType
        assertEquals label, a.label
        assertEquals 0, a.start
        assertEquals 5, a.end
        assertNotNull a.features
        assertEquals 2, a.features.size()
        assertEquals 'hello', a.features.word
        assertEquals 'NN', a.features.pos
    }
}

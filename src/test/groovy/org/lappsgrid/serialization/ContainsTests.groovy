/*
 * Copyright (C) 2017 The Language Applications Grid
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.lappsgrid.serialization

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.lappsgrid.serialization.lif.Dependency
import org.lappsgrid.serialization.lif.View

import java.time.LocalDateTime

import static org.lappsgrid.discriminator.Discriminators.*

import static org.junit.Assert.*

import org.lappsgrid.serialization.lif.Contains

/**
 * @author Keith Suderman
 */
class ContainsTests {
    View view

    @Before
    void setup() {
        view = new View()
        view.id = 'v0'
    }

    @After
    void tearDown() {
        view = null
    }

    @Test
    void justTheDefaultFields() {
        Contains c = view.addContains('T', 'producer', 'type')
        assertEquals 2, c.size()
        assertEquals 'producer', c.producer
        assertEquals 'type', c.type

        c = roundTrip(c)
        assertEquals 2, c.size()
        assertEquals 'producer', c.producer
        assertEquals 'type', c.type
    }

    @Test
    void allFieldsWithHelpers() {
        Contains c = view.addContains(Uri.POS, 'producer', 'type')
        assert 2 == c.size()
        c.url = 'url'
        c.tagSet = 'tagSet'
        c.dependsOn = [[view:'v1', type: 'T2']]
        assert 5 == c.size()
        assert 'producer' == c.producer
        assert 'type' == c.type
        assert 'url' == c.url
        assert 'tagSet' == c.getTagSet()
        assert c.dependsOn instanceof List
        assert c.dependsOn.size() == 1

        c = roundTrip(c)
        assert 5 == c.size()
        assert 'producer' == c.producer
        assert 'type' == c.type
        assert 'url' == c.url
//        assert 'tagSet' == c.tagSet
        assert 'tagSet' == c.getTagSet()
    }

    @Test
    void arbitraryField() {
        Contains c = view.addContains('T', 'producer', 'type')
        c.foo = 'bar'
        c.list = [1, 2, 3]

        assert 4 == c.size()
        assert 'producer' == c.producer
        assert 'type' == c.type
        assert 'bar' == c.foo
        assert [1,2,3] == c.list

        c = roundTrip(c)
        assert 4 == c.size()
        assert 'producer' == c.producer
        assert 'type' == c.type
        assert 'bar' == c.foo
        assert [1,2,3] == c.list
    }

    @Test
    void getAndPut() {
        Contains c = view.addContains('T', 'producer', 'type')
        c.put('foo', 'bar')
        c.put('list', [1,2,3])
        assert 4 == c.size()
        assert 'producer' == c.producer
        assert 'type' == c.type
        assert 'bar' == c.foo
        assert [1,2,3] == c.list

        c = roundTrip(c)
        assert 4 == c.size()
        assert 'producer' == c.producer
        assert 'type' == c.type
        assert 'bar' == c.get('foo')
        assert [1,2,3] == c.get('list')
    }

    @Test
    void dependsOn() {
        Contains c = view.addContains(Uri.NE, "producer", "type")
        Dependency dependency = new Dependency("v1", Uri.TOKEN)
        c.dependency(dependency)
        c.dependency("v2", Uri.NE)
        c = roundTrip(c)

        List<Dependency> dependencies = c.getDependsOn()
        assert 2 == dependencies.size()

        dependency = dependencies[0]
        assert 'v1' == dependency.view
        assert Uri.TOKEN == dependency.type

        dependency = dependencies[1]
        assert 'v2' == dependency.view
        assert Uri.NE == dependency.type
    }

    @Test
    void tagSets() {
        Contains c = view.addContains(Uri.POS, "some guy", "type")
        c.setTagSet("penn")
        c = view.addContains(Uri.NE, "another guy", "type")
        c.setTagSet("neset")

        String json = Serializer.toJson(view)
        Map map = Serializer.parse(json, HashMap)
        assert 'penn' == map.metadata.contains[Uri.POS].posTagSet
        assert 'neset' == map.metadata.contains[Uri.NE].namedEntityCategorySet
    }

//    @Test
    void print() {
        Contains c = view.addContains(Uri.RELATION, "producer", "type")
        c.dependency("v1", Uri.TOKEN)
        c.dependency("v2", Uri.NE)
        println Serializer.toPrettyJson(view)
    }

    // Serialize to a JSON string and then parse it back into an object.
    Contains roundTrip(Contains object) {
        String json = Serializer.toJson(object)
        Contains con = Serializer.parse(json, Contains)
        // because 'atType' is marked with @JsonIgnore
        con.setAtType(object.getAtType())
        return con
    }
}

//@JsonPropertyOrder(["id", "metadata", "annotations"])
//class WTF {
//    String id
//    Map annotations = [:]
//    Map metadata = [:]
//}
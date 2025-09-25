package kotlin.core.cache

import core.cache.def.impl.IfaceDefinition
import core.cache.def.impl.ComponentType
import core.cache.def.impl.IfaceDefinition.Companion.encode
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class IfaceDefTests {

    @Test fun `encode version 1 should produce expected byte array`() {
        val def = IfaceDefinition().apply {
            version = 1
            type = ComponentType.TEXT
            buttonType = 1
            clientCode = 100
            baseX = 50
            baseY = 50
            baseWidth = 200
            baseHeight = 100
            alpha = 255
            overlayer = -1
            unknownProp_11 = 0
            text = "Test"
            activeText = "Active"
        }

        val bytes = encode(def)

        Assertions.assertNotNull(bytes, "Encoded byte array should not be null")
        Assertions.assertTrue(bytes.isNotEmpty(), "Encoded byte array should not be empty")
        Assertions.assertEquals(0.toByte(), bytes[0], "First byte should be 0 for version 1 encode")
    }

    @Test fun `encode version 3 should produce expected byte array`() {
        val def = IfaceDefinition().apply {
            version = 3
            type = ComponentType.TEXT
            buttonType = 1
            clientCode = 100
            baseX = 50
            baseY = 50
            baseWidth = 200
            baseHeight = 100
            dynWidth = 10
            dynHeight = 10
            yMode = 0
            xMode = 0
            overlayer = -1
            hidden = false
            optionBase = "Option"
        }

        val bytes = encode(def)

        Assertions.assertNotNull(bytes)
        Assertions.assertTrue(bytes.isNotEmpty())
        Assertions.assertEquals(3.toByte(), bytes[0], "First byte should be 3 for version 3 encode")
    }
}

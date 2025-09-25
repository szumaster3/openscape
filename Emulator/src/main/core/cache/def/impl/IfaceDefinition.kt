package core.cache.def.impl

import core.cache.Cache.getData
import core.cache.Cache.getIndexCapacity
import core.cache.CacheIndex
import core.net.packet.IoBuffer
import core.net.packet.PacketHeader
import java.nio.ByteBuffer
import java.util.*

/**
 * The type Iface definition.
 */
class IfaceDefinition {
    var id: Int = 0
    var type: ComponentType? = null
    var version: Int = 0
    var clientCode: Int = 0
    var baseX: Int = 0
    var baseY: Int = 0
    var baseWidth: Int = 0
    var baseHeight: Int = 0
    var dynWidth: Int = 0
    var dynHeight: Int = 0
    var xMode: Int = 0
    var yMode: Int = 0
    var overlayer: Int = 0
    var hidden: Boolean = false
    var scrollMaxH: Int = 0
    var scrollMaxV: Int = 0
    var noClickThrough: Boolean = false
    var spriteId: Int = 0
    var activeSpriteId: Int = 0
    var angle2d: Int = 0
    var hasAlpha: Boolean = false
    var spriteTiling: Boolean = false
    var alpha: Int = 0
    var outlineThickness: Int = 0
    var shadowColor: Int = 0
    var hFlip: Boolean = false
    var vFlip: Boolean = false
    var modelType: Int = 0
    var activeModelType: Int = 0
    var modelId: Int = 0
    var activeModelId: Int = 0
    var unknownModelProp_1: Int = 0
    var unknownModelProp_2: Int = 0
    var modelXAngle: Int = 0
    var modelYAngle: Int = 0
    var modelYOffset: Int = 0
    var modelZoom: Int = 0
    var modelAnimId: Int = 0
    var activeModelAnimId: Int = 0
    var modelOrtho: Boolean = false
    var unknownModelProp_3: Int = 0
    var unknownModelProp_4: Int = 0
    var unknownModelProp_5: Boolean = false
    var unknownModelProp_6: Int = 0
    var unknownModelProp_7: Int = 0
    var font: Int = 0
    var text: String? = null
    var activeText: String? = null
    var vPadding: Int = 0
    var halign: Int = 0
    var valign: Int = 0
    var shadowed: Boolean = false
    var color: Int = 0
    var activeColor: Int = 0
    var overColor: Int = 0
    var unknownColor: Int = 0
    var filled: Boolean = false
    var lineWidth: Int = 0
    var unknownProp_8: Boolean = false
    var unknownIntArray_1: IntArray? = null
    var unknownIntArray_2: IntArray? = null
    var unknownByteArray_1: ByteArray? = null
    var unknownByteArray_2: ByteArray? = null
    var optionBase: String? = null
    var ops: Array<String?>? = null
    var dragDeadzone: Int = 0
    var dragDeadtime: Int = 0
    var dragRenderBehavior: Boolean = false
    var opCircumfix: String? = null
    var opSuffix: String? = null
    var option: String? = null
    var unknownProp_9: Int = 0
    var unknownProp_10: Int = 0
    var unknownProp_11: Int = 0
    var cs1ComparisonOperands: IntArray? = null
    var cs1ComparisonOpcodes: IntArray? = null
    var cs1Scripts: Array<IntArray?>? = null
    var objCounts: IntArray? = null
    var objTypes: IntArray? = null
    var invMarginX: Int = 0
    var invMarginY: Int = 0
    var invOffsetX: IntArray? = null
    var invOffsetY: IntArray? = null
    var invSprite: IntArray? = null
    var buttonType: Int = 0
    var invOptions: Array<String?>? = null
    var scripts: LinkedScripts? = null
    var triggers: ScriptTriggers? = null
    var children: Array<IfaceDefinition?>? = null
    var parent: Int = 0

    override fun toString(): String =
        if (parent == id) {
            "[IF $id (P)]"
        } else {
            "[IF " + (id shr 16) + ", CH " + (id and 0xFF) + " (" + type!!.name + ")]"
        }

    companion object {
        private val defCache = HashMap<Int, IfaceDefinition>()

        /**
         * For id iface definition.
         *
         * @param id the id
         * @return the iface definition
         */
        @JvmStatic
        fun forId(id: Int): IfaceDefinition? {
            if (defCache.containsKey(id)) {
                return defCache[id]
            }
            val def = loadAndParse(id)
            defCache[id] = def
            return def
        }

        /**
         * For id iface definition.
         *
         * @param id    the id
         * @param child the child
         * @return the iface definition
         */
        @JvmStatic
        fun forId(
            id: Int,
            child: Int,
        ): IfaceDefinition? {
            if (defCache.containsKey(child + (id shl 16))) return defCache[child + (id shl 16)]
            val def = forId(id)
            return def!!.children!![child]
        }

        private fun loadAndParse(id: Int): IfaceDefinition {
            val def = IfaceDefinition()
            val childCount = getIndexCapacity(CacheIndex.COMPONENTS)
            def.children = arrayOfNulls(childCount)
            def.id = id
            def.parent = id
            for (i in 0 until childCount) {
                def.children!![i] = parseChild(id, i)
            }
            return def
        }

        private fun parseChild(
            id: Int,
            childIndex: Int,
        ): IfaceDefinition {
            val def = IfaceDefinition()
            def.id = childIndex + (id shl 16)
            def.parent = id
            defCache[def.id] = def

            val dataRaw = getData(CacheIndex.COMPONENTS, id, childIndex) ?: return def

            val data = IoBuffer(-1, PacketHeader.NORMAL, ByteBuffer.wrap(dataRaw))

            if (dataRaw[0].toInt() == -1) {
                decodeIf3(def, data)
            } else {
                decodeIf1(def, data)
            }

            return def
        }

        private fun decodeIf1(
            def: IfaceDefinition,
            data: IoBuffer,
        ) {
            data.g1()
            def.version = 1
            def.type = ComponentType.values()[data.g1()]
            def.buttonType = data.g1()
            def.clientCode = data.g2()
            def.baseX = data.g2b()
            def.baseY = data.g2b()
            def.baseWidth = data.g2()
            def.baseHeight = data.g2()
            def.dynHeight = 0
            def.dynWidth = def.dynHeight
            def.yMode = 0
            def.xMode = def.yMode
            def.alpha = data.g1()
            def.overlayer = data.g2()
            if (def.overlayer == 65535) {
                def.overlayer = -1
            } else {
                def.overlayer += def.id and -0x10000
            }
            def.unknownProp_11 = data.g2()

            val unknownLocal_1 = data.g1()
            if (unknownLocal_1 > 0) {
                def.cs1ComparisonOpcodes = IntArray(unknownLocal_1)
                def.cs1ComparisonOperands = IntArray(unknownLocal_1)
                for (i in 0 until unknownLocal_1) {
                    def.cs1ComparisonOpcodes!![i] = data.g1()
                    def.cs1ComparisonOperands!![i] = data.g2()
                }
            }

            val unknownLocal_2 = data.g1()
            var unknownLocal_3: Int
            if (unknownLocal_2 > 0) {
                def.cs1Scripts = arrayOfNulls(unknownLocal_2)
                for (i in 0 until unknownLocal_2) {
                    unknownLocal_3 = data.g2()
                    def.cs1Scripts!![i] = IntArray(unknownLocal_3)
                    for (j in 0 until unknownLocal_3) {
                        def.cs1Scripts!![i]!![j] = data.g2()
                        if (def.cs1Scripts!![i]!![j] == 65535) def.cs1Scripts!![i]!![j] = -1
                    }
                }
            }

            var flags = 0
            if (def.type == ComponentType.SCROLLABLE) {
                def.scrollMaxV = data.g2()
                def.hidden = data.g1() == 1
            }
            if (def.type == ComponentType.UNKNOWN_1) {
                data.g2()
                data.g1()
            }
            if (def.type == ComponentType.UNKNOWN_2) {
                def.dynHeight = 3
                def.objCounts = IntArray(def.baseWidth * def.baseHeight)
                def.objTypes = IntArray(def.baseWidth * def.baseHeight)
                def.dynWidth = 3
                val unknownLocal_4 = data.g1()
                val unknownLocal_5 = data.g1()
                if (unknownLocal_4 == 1) flags = 268435456
                val unknownLocal_6 = data.g1()
                if (unknownLocal_5 == 1) flags = flags or 0x40000000
                if (unknownLocal_6 == 1) flags = flags or Int.MAX_VALUE
                val unknownLocal_7 = data.g1()
                if (unknownLocal_7 == 1) flags = flags or 0x20000000
                def.invMarginX = data.g1()
                def.invMarginY = data.g1()
                def.invOffsetX = IntArray(20)
                def.invOffsetY = IntArray(20)
                def.invSprite = IntArray(20)
                for (i in 0..19) {
                    val hasSprite = data.g1()
                    if (hasSprite == 1) {
                        def.invOffsetX!![i] = data.g2b()
                        def.invOffsetY!![i] = data.g2b()
                        def.invSprite!![i] = data.g4()
                    } else {
                        def.invSprite!![i] = -1
                    }
                }
                def.invOptions = arrayOfNulls(5)
                for (i in 0..4) {
                    val option = data.jagString
                    if (option.length > 0) {
                        def.invOptions!![i] = option
                        flags = flags or (0x1 shl i + 23)
                    }
                }
            }
            if (def.type == ComponentType.FIGURE) {
                def.filled = data.g1() == 1
            }
            if (def.type == ComponentType.TEXT || def.type == ComponentType.UNKNOWN_1) {
                def.halign = data.g1()
                def.valign = data.g1()
                def.vPadding = data.g1()
                def.font = data.g2()
                if (def.font == 65535) def.font = -1
                def.shadowed = data.g1() == 1
            }
            if (def.type == ComponentType.TEXT) {
                def.text = data.jagString
                def.activeText = data.jagString
            }
            if (def.type == ComponentType.UNKNOWN_1 ||
                def.type == ComponentType.FIGURE ||
                def.type == ComponentType.TEXT
            ) {
                def.color = data.g4()
            }
            if (def.type == ComponentType.FIGURE || def.type == ComponentType.TEXT) {
                def.activeColor = data.g4()
                def.overColor = data.g4()
                def.unknownColor = data.g4()
            }
            if (def.type == ComponentType.SPRITE) {
                def.spriteId = data.g4()
                def.activeSpriteId = data.g4()
            }
            if (def.type == ComponentType.MODEL) {
                def.modelType = 1
                def.modelId = data.g2()
                if (def.modelId == 65535) def.modelId = -1
                def.activeModelType = 1
                def.activeModelId = data.g2()
                if (def.activeModelId == 65535) def.activeModelId = -1
                def.modelAnimId = data.g2()
                if (def.modelAnimId == 65535) def.modelAnimId = -1
                def.activeModelAnimId = data.g2()
                if (def.activeModelAnimId == 65535) def.activeModelAnimId = -1
                def.modelZoom = data.g2()
                def.modelXAngle = data.g2()
                def.modelYAngle = data.g2()
            }
            if (def.type == ComponentType.UNKNOWN_7) {
                def.dynWidth = 3
                def.dynHeight = def.dynWidth
                def.objCounts = IntArray(def.baseHeight * def.baseWidth)
                def.objTypes = IntArray(def.baseHeight * def.baseWidth)
                def.halign = data.g1()
                def.font = data.g2()
                if (def.font == 65535) def.font = -1
                def.shadowed = data.g1() == 1
                def.color = data.g4()
                def.invMarginX = data.g2b()
                def.invMarginY = data.g2b()
                val invHasOptions = data.g1()
                if (invHasOptions == 1) {
                    flags = flags or 0x40000000
                }
                def.invOptions = arrayOfNulls(5)
                for (i in 0..4) {
                    val option = data.jagString
                    if (option.length > 0) {
                        def.invOptions!![i] = option
                        flags = flags or (0x1 shl i + 23)
                    }
                }
            }
            if (def.type == ComponentType.UNKNOWN_8) def.text = data.jagString
            if (def.buttonType == 2 || def.type == ComponentType.UNKNOWN_2) {
                def.opCircumfix = data.jagString
                def.opSuffix = data.jagString
                val unknownLocal_4 = data.g2() and 0x3F
                flags = flags or (unknownLocal_4 shl 11)
            }
            if (def.buttonType == 1 || def.buttonType == 4 || def.buttonType == 5 || def.buttonType == 6) {
                def.option = data.jagString
                if (def.option!!.length == 0) {
                    when (def.buttonType) {
                        1 -> def.option = "Ok"
                        4, 5 -> def.option = "Select"
                        6 -> def.option = "Continue"
                    }
                }
            }
            if (def.buttonType == 1 || def.buttonType == 4 || def.buttonType == 5) {
                flags = flags or 0x400000
            }
            if (def.buttonType == 6) {
                flags = flags or 0x1
            }
        }

        private fun decodeIf3(
            def: IfaceDefinition,
            data: IoBuffer,
        ) {
            data.g1()
            def.version = 3
            var type = data.g1()
            if ((type and 0x80) != 0) {
                type = type and 0x7F
                data.jagString
            }
            def.type = ComponentType.values()[type]
            def.clientCode = data.g2()
            def.baseX = data.g2b()
            def.baseY = data.g2b()
            def.baseWidth = data.g2()
            def.baseHeight = data.g2()
            def.dynWidth = data.g1b()
            def.dynHeight = data.g1b()
            def.yMode = data.g1b()
            def.xMode = data.g1b()
            def.overlayer = data.g2()
            if (def.overlayer == 65535) {
                def.overlayer = -1
            } else {
                def.overlayer = (def.id and -0x10000) + def.overlayer
            }
            def.hidden = data.g1() == 1
            parseIf3Type(def, data)

            val unknownLocal_1 = data.g3()
            var unknownLocal_2 = data.g1()
            var unknownLocal_3: Int
            if (unknownLocal_2 != 0) {
                def.unknownIntArray_1 = IntArray(10)
                def.unknownByteArray_1 = ByteArray(10)
                def.unknownByteArray_2 = ByteArray(10)

                while (unknownLocal_2 != 0) {
                    unknownLocal_3 = (unknownLocal_2 shr 4) - 1
                    unknownLocal_2 = data.g1() or (unknownLocal_2 shl 8)
                    unknownLocal_2 = unknownLocal_2 and 0xFFF
                    if (unknownLocal_2 == 4095) {
                        def.unknownIntArray_1!![unknownLocal_3] = -1
                    } else {
                        def.unknownIntArray_1!![unknownLocal_3] = unknownLocal_2
                    }
                    def.unknownByteArray_2!![unknownLocal_3] = data.g1b().toByte()
                    def.unknownByteArray_1!![unknownLocal_3] = data.g1b().toByte()
                    unknownLocal_2 = data.g1()
                }
            }

            def.optionBase = data.jagString
            unknownLocal_2 = data.g1()
            val opCount = unknownLocal_2 and 0xF
            if (opCount > 0) {
                def.ops = arrayOfNulls(opCount)
                for (i in 0 until opCount) {
                    def.ops!![i] = data.jagString
                }
            }

            var unknownLocal_5: Int
            val unknownLocal_6 = unknownLocal_2 shr 4
            if (unknownLocal_6 > 0) {
                unknownLocal_5 = data.g1()
                def.unknownIntArray_2 = IntArray(unknownLocal_5 + 1)
                for (i in def.unknownIntArray_2!!.indices) def.unknownIntArray_2!![i] = -1
                def.unknownIntArray_2!![unknownLocal_5] = data.g2()
            }
            if (unknownLocal_6 > 1) {
                unknownLocal_5 = data.g1()
                def.unknownIntArray_2!![unknownLocal_5] = data.g2()
            }
            def.dragDeadzone = data.g1()
            def.dragDeadtime = data.g1()
            def.dragRenderBehavior = data.g1() == 1
            unknownLocal_5 = -1
            def.opCircumfix = data.jagString

            if ((unknownLocal_1 shr 11 and 0x7F) != 0) {
                unknownLocal_5 = data.g2()
                def.unknownProp_9 = data.g2()
                if (unknownLocal_5 == 65535) unknownLocal_5 = -1
                if (def.unknownProp_9 == 65535) def.unknownProp_9 = -1
                def.unknownProp_10 = data.g2()
                if (def.unknownProp_10 == 65535) def.unknownProp_10 = -1
            }

            def.scripts = LinkedScripts()
            def.scripts!!.unknown = parseIf3ScriptArgs(data)
            def.scripts!!.onMouseOver = parseIf3ScriptArgs(data)
            def.scripts!!.onMouseLeave = parseIf3ScriptArgs(data)
            def.scripts!!.onUseWith = parseIf3ScriptArgs(data)
            def.scripts!!.onUse = parseIf3ScriptArgs(data)
            def.scripts!!.onVarpTransmit = parseIf3ScriptArgs(data)
            def.scripts!!.onInvTransmit = parseIf3ScriptArgs(data)
            def.scripts!!.onStatTransmit = parseIf3ScriptArgs(data)
            def.scripts!!.onTimer = parseIf3ScriptArgs(data)
            def.scripts!!.onOptionClick = parseIf3ScriptArgs(data)
            def.scripts!!.onMouseRepeat = parseIf3ScriptArgs(data)
            def.scripts!!.onClickRepeat = parseIf3ScriptArgs(data)
            def.scripts!!.onDrag = parseIf3ScriptArgs(data)
            def.scripts!!.onRelease = parseIf3ScriptArgs(data)
            def.scripts!!.onHold = parseIf3ScriptArgs(data)
            def.scripts!!.onDragStart = parseIf3ScriptArgs(data)
            def.scripts!!.onDragRelease = parseIf3ScriptArgs(data)
            def.scripts!!.onScroll = parseIf3ScriptArgs(data)
            def.scripts!!.onVarcTransmit = parseIf3ScriptArgs(data)
            def.scripts!!.onVarcstrTransmit = parseIf3ScriptArgs(data)

            def.triggers = ScriptTriggers()
            def.triggers!!.varpTriggers = parseIf3Triggers(data)
            def.triggers!!.inventoryTriggers = parseIf3Triggers(data)
            def.triggers!!.statTriggers = parseIf3Triggers(data)
            def.triggers!!.varcTriggers = parseIf3Triggers(data)
            def.triggers!!.varcstrTriggers = parseIf3Triggers(data)
        }

        private fun parseIf3ScriptArgs(data: IoBuffer): ScriptArgs? {
            val argCount = data.g1()
            if (argCount == 0) return null
            val argArray = arrayOfNulls<Any>(argCount)
            for (i in 0 until argCount) {
                val type = data.g1()
                if (type == 0) {
                    val datum = data.g4()
                    argArray[i] = datum
                } else if (type == 1) {
                    val datum = data.jagString
                    argArray[i] = datum
                }
            }
            return ScriptArgs(argArray[0] as Int, Arrays.copyOfRange(argArray, 1, argArray.size))
        }

        private fun parseIf3Triggers(data: IoBuffer): IntArray? {
            val triggerCount = data.g1()
            if (triggerCount == 0) return null
            val triggers = IntArray(triggerCount)
            for (i in 0 until triggerCount) {
                triggers[i] = data.g4()
            }
            return triggers
        }

        private fun parseIf3Type(
            def: IfaceDefinition,
            data: IoBuffer,
        ) {
            when (def.type) {
                ComponentType.SCROLLABLE -> {
                    def.scrollMaxH = data.g2()
                    def.scrollMaxV = data.g2()
                    def.noClickThrough = data.g1() == 1
                }

                ComponentType.SPRITE -> {
                    def.spriteId = data.g4()
                    def.angle2d = data.g2()
                    val spriteFlags = data.g1()
                    def.hasAlpha = (spriteFlags and 0x2) != 0
                    def.spriteTiling = (spriteFlags and 0x1) != 0
                    def.alpha = data.g1()
                    def.outlineThickness = data.g1()
                    def.shadowColor = data.g4()
                    def.vFlip = data.g1() == 1
                    def.hFlip = data.g1() == 1
                }

                ComponentType.MODEL -> {
                    def.modelType = 1
                    def.modelId = data.g2()
                    if (def.modelId == 65535) def.modelId = -1
                    def.unknownModelProp_1 = data.g2b()
                    def.unknownModelProp_2 = data.g2b()
                    def.modelXAngle = data.g2()
                    def.modelYAngle = data.g2()
                    def.modelYOffset = data.g2()
                    def.modelZoom = data.g2()
                    def.modelAnimId = data.g2()
                    if (def.modelAnimId == 65535) def.modelAnimId = -1
                    def.modelOrtho = data.g1() == 1
                    def.unknownModelProp_3 = data.g2()
                    def.unknownModelProp_4 = data.g2()
                    def.unknownModelProp_5 = data.g1() == 1
                    if (def.dynWidth != 0) def.unknownModelProp_6 = data.g2()
                    if (def.dynHeight != 0) def.unknownModelProp_7 = data.g2()
                }

                ComponentType.TEXT -> {
                    def.font = data.g2()
                    if (def.font == 65535) def.font = -1
                    def.text = data.jagString
                    def.vPadding = data.g1()
                    def.halign = data.g1()
                    def.valign = data.g1()
                    def.shadowed = data.g1() == 1
                    def.color = data.g4()
                }

                ComponentType.FIGURE -> {
                    def.color = data.g4()
                    def.filled = data.g1() == 1
                    def.alpha = data.g1()
                }

                ComponentType.UNKNOWN_9 -> {
                    def.lineWidth = data.g1()
                    def.color = data.g4()
                    def.unknownProp_8 = data.g1() == 1
                }

                else -> {}
            }
        }

        fun encode(def: IfaceDefinition): ByteArray {
            val buffer = IoBuffer(-1, PacketHeader.NORMAL, ByteBuffer.allocate(1024))

            when (def.version) {
                1 -> encodeIf1(def, buffer)
                3 -> encodeIf3(def, buffer)
                else -> throw IllegalArgumentException("Unsupported version: ${def.version}")
            }
            return buffer.array()
        }

        private fun encodeIf1(def: IfaceDefinition, buffer: IoBuffer) {
            buffer.p1(0)
            buffer.p1(def.type!!.ordinal)
            buffer.p1(def.buttonType)
            buffer.p2(def.clientCode)
            buffer.p2b(def.baseX)
            buffer.p2b(def.baseY)
            buffer.p2(def.baseWidth)
            buffer.p2(def.baseHeight)
            buffer.p1(def.alpha)
            buffer.p2(if (def.overlayer == -1) 65535 else def.overlayer and 0xFFFF)
            buffer.p2(def.unknownProp_11)

            if (def.cs1ComparisonOperands != null && def.cs1ComparisonOperands!!.isNotEmpty()) {
                buffer.p1(def.cs1ComparisonOperands!!.size)
                for (i in def.cs1ComparisonOperands!!.indices) {
                    buffer.p1(def.cs1ComparisonOpcodes!![i])
                    buffer.p2(def.cs1ComparisonOperands!![i])
                }
            } else buffer.p1(0)

            if (def.cs1Scripts != null && def.cs1Scripts!!.isNotEmpty()) {
                buffer.p1(def.cs1Scripts!!.size)
                for (script in def.cs1Scripts!!) {
                    buffer.p2(script!!.size)
                    for (s in script) buffer.p2(if (s == -1) 65535 else s)
                }
            } else buffer.p1(0)

            when (def.type) {
                ComponentType.SCROLLABLE -> {
                    buffer.p2(def.scrollMaxH)
                    buffer.p2(def.scrollMaxV)
                    buffer.p1(if (def.noClickThrough) 1 else 0)
                }

                ComponentType.TEXT,
                ComponentType.UNKNOWN_1 -> {
                    buffer.p2(if (def.font == -1) 65535 else def.font)
                    buffer.p1(def.vPadding)
                    buffer.p1(def.halign)
                    buffer.p1(def.valign)
                    buffer.p1(if (def.shadowed) 1 else 0)
                    buffer.p4(def.color)
                    buffer.writeJagString(def.text ?: "")
                    buffer.writeJagString(def.activeText ?: "")
                    buffer.p4(def.activeColor)
                    buffer.p4(def.overColor)
                    buffer.p4(def.unknownColor)
                }

                ComponentType.FIGURE -> {
                    buffer.p4(def.color)
                    buffer.p1(if (def.filled) 1 else 0)
                    buffer.p1(def.alpha)
                }

                ComponentType.SPRITE -> {
                    buffer.p4(def.spriteId)
                    buffer.p4(def.activeSpriteId)
                }

                ComponentType.MODEL -> {
                    buffer.p2(if (def.modelId == -1) 65535 else def.modelId)
                    buffer.p2(if (def.activeModelId == -1) 65535 else def.activeModelId)
                    buffer.p2(if (def.modelAnimId == -1) 65535 else def.modelAnimId)
                    buffer.p2(if (def.activeModelAnimId == -1) 65535 else def.activeModelAnimId)
                    buffer.p2(def.modelZoom)
                    buffer.p2(def.modelXAngle)
                    buffer.p2(def.modelYAngle)
                }

                else -> {}
            }

            if (def.buttonType in listOf(1, 4, 5, 6)) {
                buffer.writeJagString(
                    def.option ?: when (def.buttonType) {
                        1 -> "Ok"
                        4, 5 -> "Select"
                        6 -> "Continue"
                        else -> ""
                    }
                )
            }

            def.invOptions?.forEach { buffer.writeJagString(it ?: "") }
        }

        private fun encodeIf3(def: IfaceDefinition, buffer: IoBuffer) {
            buffer.p1(3)
            buffer.p1(def.type!!.ordinal)
            buffer.p2(def.clientCode)
            buffer.p2b(def.baseX)
            buffer.p2b(def.baseY)
            buffer.p2(def.baseWidth)
            buffer.p2(def.baseHeight)
            buffer.p1b(def.dynWidth)
            buffer.p1b(def.dynHeight)
            buffer.p1b(def.yMode)
            buffer.p1b(def.xMode)
            buffer.p2(if (def.overlayer == -1) 65535 else def.overlayer and 0xFFFF)
            buffer.p1(if (def.hidden) 1 else 0)

            if (def.unknownIntArray_1 != null && def.unknownIntArray_1!!.isNotEmpty()) {
                for (i in def.unknownIntArray_1!!.indices) {
                    buffer.p2b(if (def.unknownIntArray_1!![i] == -1) 4095 else def.unknownIntArray_1!![i])
                    buffer.p1b(def.unknownByteArray_2!![i].toInt())
                    buffer.p1b(def.unknownByteArray_1!![i].toInt())
                }
            }

            buffer.writeJagString(def.optionBase ?: "")

            val opCount = def.ops?.size ?: 0
            buffer.p1(opCount)
            def.ops?.forEach { buffer.writeJagString(it ?: "") }

            def.unknownIntArray_2?.forEach { buffer.p2(it) }

            buffer.p1(def.dragDeadzone)
            buffer.p1(def.dragDeadtime)
            buffer.p1(if (def.dragRenderBehavior) 1 else 0)

            buffer.writeJagString(def.opCircumfix ?: "")

            buffer.p2(if (def.unknownProp_9 == -1) 65535 else def.unknownProp_9)
            buffer.p2(if (def.unknownProp_10 == -1) 65535 else def.unknownProp_10)

            def.scripts?.let { script ->
                encodeScript(buffer, script.unknown)
                encodeScript(buffer, script.onMouseOver)
                encodeScript(buffer, script.onMouseLeave)
                encodeScript(buffer, script.onUseWith)
                encodeScript(buffer, script.onUse)
                encodeScript(buffer, script.onVarpTransmit)
                encodeScript(buffer, script.onInvTransmit)
                encodeScript(buffer, script.onStatTransmit)
                encodeScript(buffer, script.onTimer)
                encodeScript(buffer, script.onOptionClick)
                encodeScript(buffer, script.onMouseRepeat)
                encodeScript(buffer, script.onClickRepeat)
                encodeScript(buffer, script.onDrag)
                encodeScript(buffer, script.onRelease)
                encodeScript(buffer, script.onHold)
                encodeScript(buffer, script.onDragStart)
                encodeScript(buffer, script.onDragRelease)
                encodeScript(buffer, script.onScroll)
                encodeScript(buffer, script.onVarcTransmit)
                encodeScript(buffer, script.onVarcstrTransmit)
            }

            def.triggers?.let { trigger ->
                encodeTriggers(buffer, trigger.varpTriggers)
                encodeTriggers(buffer, trigger.inventoryTriggers)
                encodeTriggers(buffer, trigger.statTriggers)
                encodeTriggers(buffer, trigger.varcTriggers)
                encodeTriggers(buffer, trigger.varcstrTriggers)
            }

            when (def.type) {
                ComponentType.SPRITE -> {
                    buffer.p4(def.spriteId)
                    buffer.p2(def.angle2d)
                    var spriteFlags = 0
                    if (def.spriteTiling) spriteFlags = spriteFlags or 0x1
                    if (def.hasAlpha) spriteFlags = spriteFlags or 0x2
                    buffer.p1(spriteFlags)
                    buffer.p1(def.alpha)
                    buffer.p1(def.outlineThickness)
                    buffer.p4(def.shadowColor)
                    buffer.p1(if (def.vFlip) 1 else 0)
                    buffer.p1(if (def.hFlip) 1 else 0)
                }
                ComponentType.MODEL -> {
                    buffer.p2(if (def.modelId == -1) 65535 else def.modelId)
                    buffer.p2(if (def.activeModelId == -1) 65535 else def.activeModelId)
                    buffer.p2(if (def.modelAnimId == -1) 65535 else def.modelAnimId)
                    buffer.p2(if (def.activeModelAnimId == -1) 65535 else def.activeModelAnimId)
                    buffer.p2(def.modelZoom)
                    buffer.p2(def.modelXAngle)
                    buffer.p2(def.modelYAngle)
                    buffer.p2(def.modelYOffset)
                    buffer.p1(if (def.modelOrtho) 1 else 0)
                    buffer.p2(def.unknownModelProp_1)
                    buffer.p2(def.unknownModelProp_2)
                    buffer.p2(def.unknownModelProp_3)
                    buffer.p2(def.unknownModelProp_4)
                    buffer.p1(if (def.unknownModelProp_5) 1 else 0)
                    buffer.p2(def.unknownModelProp_6)
                    buffer.p2(def.unknownModelProp_7)
                }
                ComponentType.TEXT -> {
                    buffer.p2(if (def.font == -1) 65535 else def.font)
                    buffer.writeJagString(def.text ?: "")
                    buffer.p1(def.vPadding)
                    buffer.p1(def.halign)
                    buffer.p1(def.valign)
                    buffer.p1(if (def.shadowed) 1 else 0)
                    buffer.p4(def.color)
                    buffer.writeJagString(def.activeText ?: "")
                    buffer.p4(def.activeColor)
                    buffer.p4(def.overColor)
                    buffer.p4(def.unknownColor)
                }
                ComponentType.FIGURE -> {
                    buffer.p4(def.color)
                    buffer.p1(if (def.filled) 1 else 0)
                    buffer.p1(def.alpha)
                }
                ComponentType.SCROLLABLE -> {
                    buffer.p2(def.scrollMaxH)
                    buffer.p2(def.scrollMaxV)
                    buffer.p1(if (def.noClickThrough) 1 else 0)
                }
                ComponentType.UNKNOWN_9 -> {
                    buffer.p1(def.lineWidth)
                    buffer.p4(def.color)
                    buffer.p1(if (def.unknownProp_8) 1 else 0)
                }
                else -> {}
            }
        }

        private fun encodeScript(buffer: IoBuffer, script: ScriptArgs?) {
            if (script == null) {
                buffer.p1(0)
                return
            }

            val args = script.args
            buffer.p1(args.size)
            for (arg in args) {
                when (arg) {
                    is Int -> buffer.p1(0).p4(arg)
                    is String -> buffer.p1(1).writeJagString(arg)
                }
            }
        }

        private fun encodeTriggers(buffer: IoBuffer, triggers: IntArray?) {
            if (triggers == null) {
                buffer.p1(0)
                return
            }
            buffer.p1(triggers.size)
            for (t in triggers) buffer.p4(t)
        }
    }
}
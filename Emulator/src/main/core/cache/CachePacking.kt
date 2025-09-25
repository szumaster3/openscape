package core.cache

import com.displee.cache.CacheLibrary
import com.displee.cache.index.Index
import com.displee.cache.index.archive.Archive
import core.api.log
import core.cache.def.impl.ComponentType
import core.cache.def.impl.IfaceDefinition
import core.cache.def.impl.LinkedScripts
import core.tools.Log

class CachePacking(private val cache: CacheLibrary) {

    /**
     * Adds a new graphic component (typically a sprite) to an interface definition.
     * @param parentId The id of the parent interface (group).
     * @param index The index within the parent interface where the component will be placed.
     * @param x The X position of the component in the interface.
     * @param y The Y position of the component in the interface.
     * @param width The width of the component.
     * @param height The height of the component.
     * @param overlay The overlay type (-1 for none, or other values for layer effects).
     * @param spriteId The ID of the sprite/image used by this component.
     * @param scripts Optional [LinkedScripts] to define behaviors such as clicks, hovers, etc.
     * @return The newly created [IfaceDefinition] component.
     * @throws IllegalStateException If the parent interface is not found in the cache.
     */
    fun addGraphicComponent(
        parentId: Int,
        index: Int,
        x: Int,
        y: Int,
        width: Int,
        height: Int,
        overlay: Int = -1,
        spriteId: Int,
        scripts: LinkedScripts? = null
    ): IfaceDefinition {
        val root = IfaceDefinition.forId(parentId) ?: error("Interface [$parentId] not found!")

        val newSprite = IfaceDefinition().apply {
            this.id = (parentId shl 16) + index
            this.parent = parentId
            this.version = 3
            this.type = ComponentType.SPRITE

            this.baseX = x
            this.baseY = y
            this.baseWidth = width
            this.baseHeight = height

            this.overlayer = overlay
            this.spriteId = spriteId
            //this.activeSpriteId = spriteId
            //this.spriteTiling = false
            //this.hasAlpha = false
            this.color = 0
            //this.alpha = 0
            //this.outlineThickness = 0
            //this.shadowColor = 0
            //this.hFlip = false
            //this.vFlip = false
            this.scripts = scripts
            optionBase = ""
            ops = arrayOf("")
            invOptions = arrayOf("")
        }

        val currentChildren = root.children ?: arrayOfNulls<IfaceDefinition>(index + 1)
        val updatedChildren = Array(maxOf(currentChildren.size, index + 1)) { i ->
            currentChildren.getOrNull(i)
        }
        updatedChildren[index] = newSprite
        root.children = updatedChildren
        saveComponent(parentId, index, newSprite)
        return newSprite
    }

    /**
     * Serializes and saves a component definition into the cache.
     * @param ifaceId The id of the parent interface group.
     * @param childIndex The index of the component inside the group.
     * @param def The interface component to be saved.
     */
    private fun saveComponent(ifaceId: Int, childIndex: Int, def: IfaceDefinition) {
        val encodedBytes = IfaceDefinition.encode(def)
        val index: Index = cache.index(CacheIndex.COMPONENTS.id)
        val archive: Archive = index.archive(ifaceId) ?: index.add(ifaceId)
        archive.add(childIndex, encodedBytes, overwrite = true)
        index.update()
        cache.update()
        log(this.javaClass, Log.INFO, "Added component to interface=[$ifaceId], index=[$childIndex], type=[${def.type}]")
    }
}

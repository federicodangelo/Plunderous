package com.fangelo.libraries.sprite.system

import com.badlogic.ashley.core.Engine
import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.utils.ImmutableArray
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.g2d.TextureAtlas
import com.badlogic.gdx.math.MathUtils
import com.fangelo.libraries.camera.component.Camera
import com.fangelo.libraries.transform.Transform
import com.fangelo.libraries.render.system.VisualCameraRenderer
import com.fangelo.libraries.sprite.component.VisualSprite
import com.fangelo.libraries.utils.MutableListUtils
import ktx.ashley.allOf
import ktx.ashley.mapperFor

class VisualSpriteRenderSystem : VisualCameraRenderer() {
    private lateinit var entities: ImmutableArray<Entity>
    private var sortedEntities = mutableListOf<Entity>()

    private val transform = mapperFor<Transform>()
    private val visual = mapperFor<VisualSprite>()
    private val camera = mapperFor<Camera>()

    private lateinit var batch: SpriteBatch

    override fun addedToEngine(engine: Engine) {
        batch = SpriteBatch()
        entities = engine.getEntitiesFor(allOf(Transform::class, VisualSprite::class).get())
    }

    override fun removedFromEngine(engine: Engine) {
        batch.dispose()
    }

    override fun render(camera: Camera) {
        sortEntities()
        beginRender(camera)
        drawEntities(camera)
        endRender()
    }

    private fun sortEntities() {
        sortedEntities.clear()
        sortedEntities.addAll(entities)
        //sortedEntities.sortWith(EntitiesSorterByY)
        MutableListUtils.nonAllocatingSort(sortedEntities, EntitiesSorterByY)
    }

    companion object EntitiesSorterByY : Comparator<Entity> {

        private val transform = mapperFor<Transform>()

        override fun compare(a: Entity, b: Entity): Int {

            val y1 = transform.get(a).y
            val y2 = transform.get(b).y

            if (y1 > y2)
                return 1
            else if (y1 < y2)
                return -1

            return 0
        }
    }

    private fun drawEntities(camera: Camera) {
        var transform: Transform
        var visualSprite: VisualSprite

        for (index in 0 until sortedEntities.size) {
            val e = sortedEntities[index]

            transform = this.transform.get(e)
            visualSprite = this.visual.get(e)

            if (!camera.shouldRenderVisualComponent(visualSprite))
                continue

            for (spriteIndex in 0 until visualSprite.sprites.size) {
                val item = visualSprite.sprites[spriteIndex]

                val texture = item.texture

                var targetX = transform.x - item.width * 0.5f + item.offsetX
                var targetY = transform.y - item.height * 0.5f + item.offsetY
                var width = item.width
                var height = item.height

                if (texture is TextureAtlas.AtlasRegion) {

                    targetX += (texture.offsetX / texture.originalWidth.toFloat()) * width
                    targetY += (texture.offsetY / texture.originalHeight.toFloat()) * height

                    width *= texture.packedWidth.toFloat() / texture.originalWidth.toFloat()
                    height *= texture.packedHeight.toFloat() / texture.originalHeight.toFloat()
                }

                batch.draw(
                    texture, targetX, targetY, width * 0.5f, height * 0.5f, width, height, 1f, 1f, transform.rotation * MathUtils
                        .radiansToDegrees
                )
            }
        }
    }

    private fun beginRender(camera: Camera) {
        batch.projectionMatrix = camera.combined
        batch.begin()
    }

    private fun endRender() {
        batch.end()
    }
}
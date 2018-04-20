package com.fangelo.plunderous.client.game.ship.factory

import com.badlogic.ashley.core.Engine
import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.assets.AssetManager
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.TextureAtlas
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.BodyDef
import com.fangelo.libraries.light.component.Light
import com.fangelo.libraries.light.component.WorldLightRenderer
import com.fangelo.libraries.physics.component.Rigidbody
import com.fangelo.libraries.physics.component.World
import com.fangelo.libraries.sprite.Sprite
import com.fangelo.libraries.sprite.component.VisualSprite
import com.fangelo.libraries.transform.Transform
import com.fangelo.plunderous.client.game.constants.GameRenderFlags
import com.fangelo.plunderous.client.game.generator.component.GeneratorAreaSource
import com.fangelo.plunderous.client.game.ship.component.MainShip
import com.fangelo.plunderous.client.game.ship.component.Ship
import com.fangelo.plunderous.client.game.ship.component.ShipInput
import ktx.ashley.entity
import ktx.box2d.BodyDefinition

class ShipFactory(private val engine: Engine, private val assetManager: AssetManager, private val mainWorld: World) {

    private val lightRadius = 25.0f

    fun buildMainShip(positionX: Float, positionY: Float): Entity {

        val shipAtlas = assetManager.get<TextureAtlas>("ships/ships.atlas")
        val shipRegion = shipAtlas.findRegion("ship")

        val playerShip = engine.entity {
            with<Transform> {
                set(positionX, positionY, MathUtils.PI)
            }
            with<Rigidbody> {
                set(mainWorld, buildShipBodyDefinition())
            }
            with<Ship>()
            with<MainShip>()
            with<ShipInput>()
            with<VisualSprite> {
                add(Sprite(shipRegion, 2f, 3f, 0f, 0f))
            }
            with<Light> {
                set(mainWorld, lightRadius, Color.WHITE, 512)
            }
            with<World>()
            with<WorldLightRenderer> {
                renderFlags = GameRenderFlags.ship
            }
            with<GeneratorAreaSource> {
                followTransformRadius = 5.0f
            }
        }

        val generatorAreaSource = playerShip.getComponent(GeneratorAreaSource::class.java)
        generatorAreaSource.followTransform = playerShip.getComponent(Transform::class.java)

        val shipWorld = playerShip.getComponent(World::class.java)
        shipWorld.followTransform = playerShip.getComponent(Transform::class.java)
        shipWorld.buildBounds(2f, 3f, 0.25f)

        return playerShip
    }


    private fun buildShipBodyDefinition(): BodyDefinition {
        val shipBack = -1.25f
        val shipBackHalfWidth = 0.4f

        val shipMiddle = 0.5f
        val shipMiddleHalfWidth = 0.6f

        val shipFront = 1.45f

        val shipBodyDefinition = BodyDefinition()

        shipBodyDefinition.type = BodyDef.BodyType.DynamicBody

        shipBodyDefinition.polygon(
            Vector2(-shipBackHalfWidth, shipBack),
            Vector2(-shipMiddleHalfWidth, shipMiddle),
            Vector2(0f, shipFront),
            Vector2(shipMiddleHalfWidth, shipMiddle),
            Vector2(shipBackHalfWidth, shipBack)
        ) {
            density = 1f
            restitution = 0f
            friction = 0.2f
        }
        shipBodyDefinition.linearDamping = 0.5f
        shipBodyDefinition.angularDamping = 0.9f
        return shipBodyDefinition
    }
}
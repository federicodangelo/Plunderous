package com.fangelo.plunderous.client.game.island.factory

import com.badlogic.ashley.core.Engine
import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.physics.box2d.BodyDef
import com.fangelo.libraries.physics.component.Rigidbody
import com.fangelo.libraries.physics.component.World
import com.fangelo.libraries.transform.Transform
import com.fangelo.plunderous.client.game.island.component.Island
import com.fangelo.plunderous.client.game.island.component.VisualIsland
import ktx.ashley.entity
import ktx.box2d.BodyDefinition

class IslandFactory(private val engine: Engine) {

    fun buildIsland(x: Float, y: Float, radius: Float, world: World): Entity {

        val island = engine.entity {
            with<Transform> {
                set(x, y, 0f)
            }
            with<Rigidbody> {
                set(world, buildIslandBodyDefinition(radius))
            }
            with<Island> {
                set(radius)
            }
            with<VisualIsland> {
                set(Color.CORAL)
            }
        }

        return island
    }

    private fun buildIslandBodyDefinition(radius: Float): BodyDefinition {
        var islandBodyDefinition = BodyDefinition()

        islandBodyDefinition.type = BodyDef.BodyType.StaticBody
        islandBodyDefinition.circle(radius)

        return islandBodyDefinition
    }
}
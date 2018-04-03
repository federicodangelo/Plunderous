package com.fangelo.libraries.camera.system

import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.systems.IteratingSystem
import com.badlogic.gdx.math.MathUtils
import com.fangelo.libraries.camera.component.Camera
import com.fangelo.libraries.transform.Transform
import ktx.ashley.allOf
import ktx.ashley.mapperFor

class UpdateCameraSystem : IteratingSystem(allOf(Transform::class, Camera::class).get()) {
    private val transform = mapperFor<Transform>()
    private val camera = mapperFor<Camera>()

    public override fun processEntity(entity: Entity, deltaTime: Float) {
        val transform = transform.get(entity)
        val camera = camera.get(entity)

        val followTransform = camera.followTransform

        if (followTransform != null) {


            transform.set(
                followTransform.x + camera.followTransformOffset.x,
                followTransform.y + camera.followTransformOffset.y,
                if (camera.followTransformRotation) followTransform.rotation + camera.followTransformRotationOffset else MathUtils.PI
            )
        }

        camera.update(transform.x, transform.y, transform.rotation)
    }
}
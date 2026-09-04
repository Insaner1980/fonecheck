package com.insaner.fonecheck.ui

import androidx.annotation.StringRes
import androidx.compose.runtime.Stable
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.ui.graphics.vector.ImageVector

data class TopBarAction(
    val icon: ImageVector,
    @StringRes val contentDescriptionResId: Int,
    val enabled: Boolean = true,
    val onClick: () -> Unit,
)

@Stable
interface TopBarActionRegistry {
    fun register(
        owner: Any,
        action: TopBarAction,
    )

    fun unregister(owner: Any)

    object NoOp : TopBarActionRegistry {
        override fun register(
            owner: Any,
            action: TopBarAction,
        ) = Unit

        override fun unregister(owner: Any) = Unit
    }
}

class TopBarActionHostState {
    private val registrations = mutableStateMapOf<Any, Registration>()

    fun registryFor(routeOwner: Any): TopBarActionRegistry =
        object : TopBarActionRegistry {
            override fun register(
                owner: Any,
                action: TopBarAction,
            ) {
                registrations[routeOwner] = Registration(owner, action)
            }

            override fun unregister(owner: Any) {
                if (registrations[routeOwner]?.componentOwner === owner) {
                    registrations.remove(routeOwner)
                }
            }
        }

    fun actionFor(routeOwner: Any?): TopBarAction? = routeOwner?.let { registrations[it]?.action }

    private data class Registration(
        val componentOwner: Any,
        val action: TopBarAction,
    )
}

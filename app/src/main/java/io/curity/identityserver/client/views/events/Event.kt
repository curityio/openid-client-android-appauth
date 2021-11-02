package io.curity.identityserver.client.views.events

/*
 * A simple event class used by view models to notify the view
 */
open class Event<T>(private val data: T) {

    var handled = false
        private set

    fun getData(): T? {
        return if (handled) {
            null
        } else {
            handled = true
            data
        }
    }
}
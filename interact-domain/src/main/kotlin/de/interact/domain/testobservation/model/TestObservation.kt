package de.interact.domain.testobservation.model

data class TestObservation(
    var observedComponents: Collection<ObservedComponent> = listOf()
) {
    fun addObservedComponent(name: ComponentName, version: ComponentVersion): ObservedComponent {
        val component = ObservedComponent(name, version)
        val result = observedComponents.firstOrNull { it == component }
        return if (result != null) {
            result
        } else {
            observedComponents += component
            component
        }
    }
}
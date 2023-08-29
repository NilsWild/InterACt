package de.rwth.swc.interact.test

import de.rwth.swc.interact.domain.ComponentName
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class PropertiesBasedComponentInformationLoaderTest {

    @Test
    fun `can load properties form interact properties file`() {
        val loader = PropertiesBasedComponentInformationLoader
        assertThat(loader.getComponentName()).isEqualTo(ComponentName("PropertiesBasedComponentInformationLoader"))
        assertThat(loader.getComponentVersion().toString()).isEqualTo("1.0.0")
    }
}
package de.interact.domain.specification.config

import de.interact.domain.specification.service.ExpectationsManager

data object Configuration {
    var expectationsManager: ExpectationsManager? = null
}

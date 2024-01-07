package de.rwth.swc.interact.controller.systemexpectations.service

import de.rwth.swc.interact.controller.persistence.service.ComponentDao
import de.rwth.swc.interact.controller.persistence.service.InterfaceExpectationDao
import de.rwth.swc.interact.controller.persistence.service.SystemPropertyExpectationDao
import de.rwth.swc.interact.domain.*
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class SystemPropertyExpectationService(
    val componentDao: ComponentDao,
    val systemPropertyExpectationDao: SystemPropertyExpectationDao,
    val interfaceExpectationDao: InterfaceExpectationDao
) {

    fun storeSystemPropertyExpectation(component: Component) {
        val componentId = createComponentIfItDoesNotExist(component)
        val systemPropertyExpectations = createSystemPropertyExpectationsIfTheyDoNotExist(
            componentId,
            component.systemPropertyExpectations
        )
        systemPropertyExpectations.forEach {
            createInterfaceExpectationIfItDoesNotExist(
                it.id!!,
                it.fromInterface!!
            )
            createInterfaceExpectationIfItDoesNotExist(
                it.id!!,
                it.toInterface!!
            )
        }
    }

    private fun createInterfaceExpectationIfItDoesNotExist(id: SystemPropertyExpectationId, interfaceExpectation: InterfaceExpectation) {
        val foundId = interfaceExpectationDao.findBySystemPropertyExpectationIdAndType(
            id,
            interfaceExpectation.javaClass
        )?.id
        if(foundId == null) {
            interfaceExpectationDao.save(interfaceExpectation).let { newId ->
                systemPropertyExpectationDao.addExpectation(id, newId, interfaceExpectation.javaClass)
                interfaceExpectation.id = newId
            }
        } else {
            interfaceExpectation.id = foundId
        }
    }

    private fun createSystemPropertyExpectationsIfTheyDoNotExist(
        componentId: ComponentId,
        systemPropertyExpectations: MutableSet<SystemPropertyExpectation>
    ): List<SystemPropertyExpectation> {
        return systemPropertyExpectations.map {
           it.apply {
               val id = systemPropertyExpectationDao.findByComponentIdAndSourceAndName(
                   componentId,
                   this.source,
                   this.name
               )?.id
               if (id == null) {
                   systemPropertyExpectationDao.save(this).let { newId ->
                       this.id = newId
                       componentDao.addSystemPropertyExpectation(componentId, newId)
                   }
               } else {
                   this.id = id
               }
           }
        }
    }

    private fun createComponentIfItDoesNotExist(component: Component): ComponentId {
        return componentDao.findIdByNameAndVersion(component.name, component.version)
            ?: componentDao.save(component)
    }
}
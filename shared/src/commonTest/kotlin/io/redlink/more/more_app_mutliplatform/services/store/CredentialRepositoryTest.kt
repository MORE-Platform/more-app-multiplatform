/*
 * Copyright LBI-DHP and/or licensed to LBI-DHP under one or more
 * contributor license agreements (LBI-DHP: Ludwig Boltzmann Institute
 * for Digital Health and Prevention -- A research institute of the
 * Ludwig Boltzmann Gesellschaft, Österreichische Vereinigung zur
 * Förderung der wissenschaftlichen Forschung).
 * Licensed under the Apache 2.0 license with Commons Clause
 * (see https://www.apache.org/licenses/LICENSE-2.0 and
 * https://commonsclause.com/).
 */
package io.redlink.more.more_app_mutliplatform.services.store

import io.redlink.more.more_app_mutliplatform.models.CredentialModel
import kotlin.test.*


class CredentialRepositoryTest {


    @Test
    fun testStore() {
        val storage = ImMemoryStorageRepository()

        val repo = CredentialRepository(storage)
        assertFalse("Empty Start") { repo.hasCredentials() }
        assertNull(repo.credentials())

        val credentials = CredentialModel("secretApiId", "secretApiKey")
        assertTrue { repo.store(credentials) }
        assertEquals(credentials, repo.credentials())

        val newRepo = CredentialRepository(storage)
        assertTrue { newRepo.hasCredentials() }
        assertEquals(credentials, newRepo.credentials())

    }

}
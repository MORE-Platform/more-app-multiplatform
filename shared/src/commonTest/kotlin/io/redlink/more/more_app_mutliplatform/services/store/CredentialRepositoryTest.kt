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
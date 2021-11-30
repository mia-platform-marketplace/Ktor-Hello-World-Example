package eu.miaplatform.services

import assertk.assertThat
import assertk.assertions.isEqualTo
import eu.miaplatform.commons.client.CrudClientInterface
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.throwable.shouldHaveMessage
import io.mockk.clearAllMocks
import io.mockk.coEvery
import io.mockk.coVerifySequence
import io.mockk.mockk
import org.junit.jupiter.api.assertThrows

class HelloWorldServiceTest : DescribeSpec({
    val crud = mockk< CrudClientInterface>()

    beforeEach { clearAllMocks() }

    describe("getBooksByHeaders") {
        it("returns books") {
            val expected = listOf("book1", "book2")

            coEvery {
                crud.getBooks(any())
            }.returns(expected)

            val service = HelloWorldService(crud)
            val result = service.getBooksByHeaders(mapOf())

            assertThat(result).isEqualTo(expected)
            coVerifySequence { crud.getBooks(mapOf()) }
        }

        it("throws if crud throws") {
            coEvery {
                crud.getBooks(any())
            }.throws(Exception())

            val service = HelloWorldService(crud)
            assertThrows<Exception> { service.getBooksByHeaders(mapOf()) }.shouldHaveMessage("books call failed")
            coVerifySequence { crud.getBooks(mapOf()) }
        }
    }
})
package eu.miaplatform.services

import eu.miaplatform.commons.client.CrudClientInterface

class HelloWorldService(
    private val crudClient: CrudClientInterface
) {
    suspend fun getBooksByHeaders(headers: Map<String, String>): List<String> {
        return try {
            crudClient.getBooks(headers)
        } catch (e: Exception) {
            throw Exception("books call failed")
        }
    }
}
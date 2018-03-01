package fr.ybonnel.framework

import io.restassured.RestAssured
import io.restassured.response.Response
import io.restassured.response.ValidatableResponse
import io.restassured.specification.RequestSpecification
import org.hamcrest.Matcher


class RestTest(block: RestTest.() -> Unit) {
    
    init {
        this.block()
    }
    
    var request: RequestSpecification? = null
    
    var response: Response? = null

    fun Given(block: GivenContext.() -> Unit) {
        check(response == null)
        val requestGiven = RestAssured.given()
        GivenContext(requestGiven).block()
        request = requestGiven
    }
    
    fun When(block: WhenContext.() -> Unit) {
        check(response == null)
        if (request == null) {
            Given { }
        }
        val whenContext = WhenContext(request!!)
        whenContext.block()
        response = whenContext.response
    } 
    
    fun Then(block: ThenContext.() -> Unit) {
        check(response != null)
        ThenContext(response!!.then()).block()
    }
    
}

class GivenContext(val request: RequestSpecification) {
    
    fun Body(body: String) {
        request.body(body)
    }
    
    fun ContentType(contentType: String) {
        request.contentType(contentType)
    }
    
    
    
}

class WhenContext(val request: RequestSpecification) {
    
    var response:Response? = null

    fun Post(url: String) {
        check(response == null)
        response = request.post(url)
    }

    fun Put(url: String) {
        check(response == null)
        response = request.put(url)
    }

    fun Delete(url: String) {
        check(response == null)
        response = request.delete(url)
    }

    fun Get(url: String) {
        check(response == null)
        response = request.get(url)
    }

}

class ThenContext(val response: ValidatableResponse) {
    fun StatusCode(code: Int) {
        response.statusCode(code)
    }
    
    fun Body(path: String, matcher: Matcher<Any>) {
        response.body(
                path,
                matcher
        )
    }
}
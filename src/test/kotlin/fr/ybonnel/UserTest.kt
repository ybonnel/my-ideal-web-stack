package fr.ybonnel

import fr.ybonnel.framework.RestTest
import io.restassured.path.json.JsonPath
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.notNullValue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.util.*

@ExtendWith(TestServer::class)
class UserTest {
    
    @Test
    fun `🚫️ I can't create a user without login`() {

        RestTest {
            Given {
                ContentType("application/json")
                Body("{\"password\": \"password\", \"salt\": \"salt\"}")
            }
            When {
                Post("http://localhost:$port/services/users")
            }
            Then {
                StatusCode(400)
            }
        }
    }

    @Test
    fun `🚫️ I can't create a user without password`() {

        RestTest {
            Given {
                ContentType("application/json")
                Body("{\"login\": \"login\", \"salt\": \"salt\"}")
            }
            When {
                Post("http://localhost:$port/services/users")
            }
            Then {
                StatusCode(400)
            }
        }
    }

    @Test
    fun `🚫️ I can't create a user without salt`() {

        RestTest {
            Given {
                ContentType("application/json")
                Body("{\"login\": \"login\", \"password\": \"password\"}")
            }
            When {
                Post("http://localhost:$port/services/users")
            }
            Then {
                StatusCode(400)
            }
        }
    }
    
    @Test
    fun `🆗 I can create a user with all required infos`() {
        RestTest {
            Given {
                ContentType("application/json")
                Body("{\"login\": \"login\", \"password\": \"password\", \"salt\": \"salt\"}")
            }
            When {
                Post("http://localhost:$port/services/users")
            }
            Then {
                StatusCode(200)
                Body("login", equalTo("login"))
                Body("password", equalTo("password"))
                Body("salt", equalTo("salt"))
                Body("id", notNullValue())
            }
        }
    }
    
    @Test
    fun `🆗 I can create a user and retrieve it`() {
        val responseBody = RestTest {
            Given {
                ContentType("application/json")
                Body("{\"login\": \"login\", \"password\": \"password\", \"salt\": \"salt\"}")
            }
            When {
                Post("http://localhost:$port/services/users")
            }
            Then {
                StatusCode(200)
                Body("id", notNullValue())
            }
        }.response!!.asString()
        
        val id = JsonPath.from(responseBody).get<String>("id")!!

        RestTest {
            When {
                Get("http://localhost:$port/services/users/$id")
            }
            Then {
                StatusCode(200)
                Body("login", equalTo("login"))
                Body("password", equalTo("password"))
                Body("salt", equalTo("salt"))
                Body("id", equalTo(id))
            }
        }
    }
    
    
    
    @Test
    fun `🆗 I can modify a user`() {
        // create the user
        val responseBody = RestTest {
            Given {
                ContentType("application/json")
                Body("{\"login\": \"login\", \"password\": \"password\", \"salt\": \"salt\"}")
            }
            When {
                Post("http://localhost:$port/services/users")
            }
            Then {
                StatusCode(200)
                Body("id", notNullValue())
            }
        }.response!!.asString()
        val id = JsonPath.from(responseBody).get<String>("id")!!

        RestTest {
            Given {
                ContentType("application/json")
                Body("{\"login\": \"login2\", \"password\": \"password2\", \"salt\": \"salt2\"}")
            }
            When {
                Put("http://localhost:$port/services/users/$id")
            }
            Then {
                StatusCode(200)
                Body("login", equalTo("login2"))
                Body("password", equalTo("password2"))
                Body("salt", equalTo("salt2"))
                Body("id", equalTo(id))
            }
        }

        RestTest {
            When {
                Get("http://localhost:$port/services/users/$id")
            }
            Then {
                StatusCode(200)
                Body("login", equalTo("login2"))
                Body("password", equalTo("password2"))
                Body("salt", equalTo("salt2"))
                Body("id", equalTo(id))
            }
        }
    }



    @Test
    fun `🚫️ I can't get a user which doesn't exists`() {

        RestTest {
            When {
                Get("http://localhost:$port/services/users/${UUID.randomUUID()}")
            }
            Then {
                StatusCode(404)
            }
        }
    }



    @Test
    fun `🆗 I can delete an existing user`() {
        val responseBody = RestTest {
            Given {
                ContentType("application/json")
                Body("{\"login\": \"login\", \"password\": \"password\", \"salt\": \"salt\"}")
            }
            When {
                Post("http://localhost:$port/services/users")
            }
            Then {
                StatusCode(200)
                Body("id", notNullValue())
            }
        }.response!!.asString()

        val id = JsonPath.from(responseBody).get<String>("id")!!

        RestTest {
            When {
                Get("http://localhost:$port/services/users/$id")
            }
            Then {
                StatusCode(200)
                Body("login", equalTo("login"))
                Body("password", equalTo("password"))
                Body("salt", equalTo("salt"))
                Body("id", equalTo(id))
            }
        }

        RestTest {
            When {
                Delete("http://localhost:$port/services/users/$id")
            }
            Then {
                StatusCode(200)
                Body("login", equalTo("login"))
                Body("password", equalTo("password"))
                Body("salt", equalTo("salt"))
                Body("id", equalTo(id))
            }
        }

        RestTest {
            When {
                Get("http://localhost:$port/services/users/$id")
            }
            Then {
                StatusCode(404)
            }
        }
    }
    
}
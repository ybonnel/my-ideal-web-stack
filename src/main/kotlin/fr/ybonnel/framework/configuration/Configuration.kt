package fr.ybonnel.framework.configuration

import java.util.*
import java.util.concurrent.ConcurrentHashMap

object Configuration {
    
    private val applicationMode = System.getProperty("application.mode", "dev")
    private val propertiesCache = ConcurrentHashMap<String, String>()
    private val properties = loadProperties()

    

    private fun loadProperties(): Properties {
        val properties = Properties()
        propertiesCache["application.mode"] = applicationMode
        Configuration::class.java.getResourceAsStream("/application.properties").use { 
            properties.load(it)
        }
        return properties
    }
    
    fun getMode(): String {
        return applicationMode
    }
    
    private fun loadProperty(key: String): String? {
        val value = properties.getProperty("%$applicationMode.$key") ?: properties.getProperty(key)
        if (value != null) {
            propertiesCache[key] = value
        }
        return value
    }
    
    fun getProperty(key: String): String? {
        return propertiesCache[key] ?: loadProperty(key) 
    }
    
    fun getProperty(key:String, defaultValue: String): String {
        return getProperty(key) ?: defaultValue
    }
    
}
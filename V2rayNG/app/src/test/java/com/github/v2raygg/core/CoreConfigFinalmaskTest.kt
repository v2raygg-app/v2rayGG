package com.github.v2raygg.core

import com.google.gson.JsonObject
import com.github.v2raygg.dto.V2rayConfig
import com.github.v2raygg.util.JsonUtil
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class CoreConfigFinalmaskTest {

    private val expectedFinalmaskJson = """{"tcp":[{"type":"fragment","settings":{"packets":"tlshello","lengths":["5","94","1"],"delays":["0"],"maxSplit":"0"}},{"type":"fragment","settings":{"packets":"1-1","lengths":["109","1"],"delays":["1"],"maxSplit":"355"}}]}"""
    private val expectedCipherSuites = "TLS_AES_256_GCM_SHA384:TLS_CHACHA20_POLY1305_SHA256:TLS_AES_128_GCM_SHA256:TLS_ECDHE_ECDSA_WITH_AES_256_GCM_SHA384:TLS_ECDHE_RSA_WITH_AES_256_GCM_SHA384:TLS_ECDHE_ECDSA_WITH_AES_128_GCM_SHA256:TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256:TLS_ECDHE_ECDSA_WITH_CHACHA20_POLY1305_SHA256:TLS_ECDHE_RSA_WITH_CHACHA20_POLY1305_SHA256:TLS_ECDHE_ECDSA_WITH_AES_256_CBC_SHA:TLS_ECDHE_RSA_WITH_AES_256_CBC_SHA:TLS_ECDHE_ECDSA_WITH_AES_128_CBC_SHA256:TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA256"
    private val expectedFingerprint = "unsafe"

    @Test
    fun testFinalmaskOverride_withSpeedtestAddress() {
        val inputJson = """
        {
          "outbounds": [
            {
              "tag": "proxy",
              "protocol": "vless",
              "settings": {
                "vnext": [
                  {
                    "address": "www.speedtest.net",
                    "port": 443,
                    "users": [
                      {
                        "id": "9a3d2949-1d1d-46f6-811f-bc662d0ef911",
                        "encryption": "none",
                        "flow": ""
                      }
                    ]
                  }
                ]
              },
              "streamSettings": {
                "network": "xhttp",
                "security": "tls",
                "tlsSettings": {
                  "serverName": "blacksugar.cfd",
                  "fingerprint": "chrome"
                },
                "finalmask": {
                  "tcp": [
                    {
                      "type": "fragment",
                      "settings": {
                        "delay": "0",
                        "length": "1",
                        "packets": "tlshello"
                      }
                    }
                  ]
                }
              }
            }
          ]
        }
        """.trimIndent()

        val jsonObject = JsonUtil.parseString(inputJson)!!.asJsonObject

        // Invoke private applyFinalmaskOverride(JsonObject) via reflection
        val method = CoreConfigManager::class.java.getDeclaredMethod("applyFinalmaskOverride", JsonObject::class.java)
        method.isAccessible = true
        method.invoke(CoreConfigManager, jsonObject)

        val outbounds = jsonObject.getAsJsonArray("outbounds")
        val firstOutbound = outbounds.get(0).asJsonObject
        val streamSettings = firstOutbound.getAsJsonObject("streamSettings")
        val finalmask = streamSettings.getAsJsonObject("finalmask")
        val tlsSettings = streamSettings.getAsJsonObject("tlsSettings")

        assertNotNull(finalmask)
        val expectedElement = JsonUtil.parseString(expectedFinalmaskJson)
        assertEquals(expectedElement, finalmask)

        assertEquals(expectedFingerprint, tlsSettings.get("fingerprint").asString)
        assertEquals(expectedCipherSuites, tlsSettings.get("cipherSuites").asString)
    }

    @Test
    fun testFinalmaskOverride_withDirectAddress() {
        val inputJson = """
        {
          "outbounds": [
            {
              "tag": "proxy",
              "protocol": "vless",
              "settings": {
                "address": "188.114.97.6",
                "port": 443
              },
              "streamSettings": {
                "security": "tls",
                "tlsSettings": {
                  "serverName": "orgtgju.org",
                  "fingerprint": "chrome"
                }
              }
            }
          ]
        }
        """.trimIndent()

        val jsonObject = JsonUtil.parseString(inputJson)!!.asJsonObject

        val method = CoreConfigManager::class.java.getDeclaredMethod("applyFinalmaskOverride", JsonObject::class.java)
        method.isAccessible = true
        method.invoke(CoreConfigManager, jsonObject)

        val outbounds = jsonObject.getAsJsonArray("outbounds")
        val firstOutbound = outbounds.get(0).asJsonObject
        val streamSettings = firstOutbound.getAsJsonObject("streamSettings")
        val finalmask = streamSettings.getAsJsonObject("finalmask")
        val tlsSettings = streamSettings.getAsJsonObject("tlsSettings")

        assertNotNull(finalmask)
        val expectedElement = JsonUtil.parseString(expectedFinalmaskJson)
        assertEquals(expectedElement, finalmask)

        assertEquals(expectedFingerprint, tlsSettings.get("fingerprint").asString)
        assertEquals(expectedCipherSuites, tlsSettings.get("cipherSuites").asString)
    }

    @Test
    fun testFinalmaskOverride_withNonMatchingAddress_doesNotOverride() {
        val inputJson = """
        {
          "outbounds": [
            {
              "tag": "proxy",
              "protocol": "vless",
              "settings": {
                "vnext": [
                  {
                    "address": "example.com",
                    "port": 443,
                    "users": [
                      {
                        "id": "test-id"
                      }
                    ]
                  }
                ]
              },
              "streamSettings": {
                "network": "tcp",
                "security": "tls",
                "tlsSettings": {
                  "fingerprint": "chrome"
                }
              }
            }
          ]
        }
        """.trimIndent()

        val jsonObject = JsonUtil.parseString(inputJson)!!.asJsonObject

        val method = CoreConfigManager::class.java.getDeclaredMethod("applyFinalmaskOverride", JsonObject::class.java)
        method.isAccessible = true
        method.invoke(CoreConfigManager, jsonObject)

        val outbounds = jsonObject.getAsJsonArray("outbounds")
        val firstOutbound = outbounds.get(0).asJsonObject
        val streamSettings = firstOutbound.getAsJsonObject("streamSettings")
        val finalmask = streamSettings.get("finalmask")
        val tlsSettings = streamSettings.getAsJsonObject("tlsSettings")

        assertNull(finalmask)
        assertEquals("chrome", tlsSettings.get("fingerprint").asString)
        assertNull(tlsSettings.get("cipherSuites"))
    }

    @Test
    fun testFinalmaskOverride_v2rayConfigObject() {
        val v2rayConfig = V2rayConfig(
            log = V2rayConfig.LogBean(),
            inbounds = arrayListOf(),
            routing = V2rayConfig.RoutingBean(domainMatcher = "hybrid", domainStrategy = "IPIfNonMatch", rules = arrayListOf()),
            outbounds = arrayListOf(
                V2rayConfig.OutboundBean(
                    protocol = "vless",
                    settings = V2rayConfig.OutboundBean.OutSettingsBean(
                        vnext = listOf(
                            V2rayConfig.OutboundBean.OutSettingsBean.VnextBean(
                                address = "www.speedtest.net",
                                port = 443,
                                users = listOf(
                                    V2rayConfig.OutboundBean.OutSettingsBean.VnextBean.UsersBean(id = "uuid")
                                )
                            )
                        )
                    ),
                    streamSettings = V2rayConfig.OutboundBean.StreamSettingsBean(
                        security = "tls",
                        tlsSettings = V2rayConfig.OutboundBean.StreamSettingsBean.TlsSettingsBean(
                            fingerprint = "chrome"
                        )
                    )
                )
            )
        )

        val method = CoreConfigManager::class.java.getDeclaredMethod("applyFinalmaskOverride", V2rayConfig::class.java)
        method.isAccessible = true
        method.invoke(CoreConfigManager, v2rayConfig)

        val streamSettings = v2rayConfig.outbounds.first().streamSettings
        val finalmask = streamSettings?.finalmask
        val tlsSettings = streamSettings?.tlsSettings

        assertNotNull(finalmask)
        val expectedElement = JsonUtil.parseString(expectedFinalmaskJson)
        assertEquals(expectedElement, finalmask)

        assertEquals(expectedFingerprint, tlsSettings?.fingerprint)
        assertEquals(expectedCipherSuites, tlsSettings?.cipherSuites)
    }
}

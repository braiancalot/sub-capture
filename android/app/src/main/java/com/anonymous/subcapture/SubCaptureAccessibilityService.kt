package com.anonymous.subcapture

import android.accessibilityservice.AccessibilityService
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo

class SubCaptureAccessibilityService : AccessibilityService() {
    companion object {
        private var instance: SubCaptureAccessibilityService? = null

        fun captureCurrentSubtitle(): String {
            if (instance == null) {
                Log.e("AccessibilityService", "Falha na captura: a instância do serviço é NULA.")
                return "Falha: Instância do serviço nula"
            }

            val rootNode = instance?.rootInActiveWindow
            if (rootNode == null) {
                Log.e("AccessibilityService", "Falha na captura: rootInActiveWindow é NULO.")
                return "Falha: Não foi possível acessar a janela"
            }

            val allTexts = mutableListOf<String>()
            findTextInNode(rootNode, allTexts)

            val knownUiTexts = setOf<String>()
            val potentialSubtitles = allTexts.filter { it.length > 5 && it !in knownUiTexts && !it.matches(Regex(".*\\d{1,2}:\\d{2}.*")) }

            return potentialSubtitles.maxByOrNull { it.length } ?: "Nenhuma legenda encontrada"
        }

        private fun findTextInNode(nodeInfo: AccessibilityNodeInfo?, texts: MutableList<String>) {
            if (nodeInfo == null) return
            if (!nodeInfo.text.isNullOrBlank()) {
                texts.add(nodeInfo.text.toString())
            }
            for (i in 0 until nodeInfo.childCount) {
                findTextInNode(nodeInfo.getChild(i), texts)
            }
        }
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {}

    override fun onInterrupt() {
        instance = null
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        instance = this
    }

    override fun onDestroy() {
        super.onDestroy()
        instance = null
    }
}

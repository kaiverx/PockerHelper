package com.pokerhelper.app.data

import android.content.Context
import android.content.SharedPreferences
import com.pokerhelper.core.model.HandOutcome
import com.pokerhelper.core.model.PlayedHand
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONArray
import org.json.JSONObject

class HandHistoryRepository(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val _hands = MutableStateFlow(loadAll())
    val hands: StateFlow<List<PlayedHand>> = _hands.asStateFlow()

    fun save(hand: PlayedHand) {
        val updated = (listOf(hand) + _hands.value).take(MAX_HANDS)
        _hands.value = updated
        persist(updated)
    }

    fun clear() {
        _hands.value = emptyList()
        prefs.edit().remove(KEY_HANDS).apply()
    }

    private fun loadAll(): List<PlayedHand> {
        val json = prefs.getString(KEY_HANDS, null) ?: return emptyList()
        return try {
            val array = JSONArray(json)
            (0 until array.length()).map { i ->
                val obj = array.getJSONObject(i)
                PlayedHand(
                    timestamp = obj.getLong("timestamp"),
                    holeCards = obj.getJSONArray("holeCards").toStringList(),
                    boardCards = obj.getJSONArray("boardCards").toStringList(),
                    outcome = HandOutcome.valueOf(obj.getString("outcome")),
                    equityPercent = if (obj.isNull("equityPercent")) null
                                    else obj.getDouble("equityPercent"),
                    opponents = obj.getInt("opponents"),
                    finalHandCategory = if (obj.isNull("finalHandCategory")) null
                                        else obj.getString("finalHandCategory")
                )
            }
        } catch (e: Exception) {
            // Если хранилище повреждено (изменился формат после обновления) —
            // обнуляем, чем падать. История — не критичные данные.
            emptyList()
        }
    }

    private fun persist(hands: List<PlayedHand>) {
        val array = JSONArray()
        hands.forEach { h ->
            val obj = JSONObject().apply {
                put("timestamp", h.timestamp)
                put("holeCards", JSONArray(h.holeCards))
                put("boardCards", JSONArray(h.boardCards))
                put("outcome", h.outcome.name)
                put("equityPercent", h.equityPercent ?: JSONObject.NULL)
                put("opponents", h.opponents)
                put("finalHandCategory", h.finalHandCategory ?: JSONObject.NULL)
            }
            array.put(obj)
        }
        prefs.edit().putString(KEY_HANDS, array.toString()).apply()
    }

    private fun JSONArray.toStringList(): List<String> =
        (0 until length()).map { getString(it) }

    companion object {
        private const val PREFS_NAME = "poker_helper_history"
        private const val KEY_HANDS = "hands"
        private const val MAX_HANDS = 500
    }
}

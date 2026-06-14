package com.example.data

import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class ProtocolField(
    val key: String,
    val prompt: String,
    val helperText: String
)

data class ProtocolDefinition(
    val key: String,
    val title: String,
    val tagline: String,
    val description: String,
    val fields: List<ProtocolField>,
    val iconName: String, // Material Icon Identifier
    val themeColorName: String // Visual tint name for styling
) {
    /**
     * Helper to compile a set of field responses into a beautifully formatted Markdown string.
     */
    fun compileMarkdown(responses: Map<String, String>, timestamp: Long): String {
        val sdf = SimpleDateFormat("EEEE, MMMM d, yyyy 'at' h:mm a", Locale.getDefault())
        val dateStr = sdf.format(Date(timestamp))
        val sb = StringBuilder()
        
        sb.append("# $title\n")
        sb.append("*Recorded on $dateStr*\n\n")
        sb.append("---\n\n")
        
        for (field in fields) {
            val ans = responses[field.key]?.trim() ?: ""
            val displayAns = if (ans.isEmpty()) "*No response provided.*" else ans
            sb.append("### ${field.prompt}\n")
            sb.append("$displayAns\n\n")
        }
        
        return sb.toString()
    }

    companion object {
        val GRATITUDE = ProtocolDefinition(
            key = "GRATITUDE",
            title = "Gratitude & Growth",
            tagline = "Ground yourself on abundance and micro-joys",
            description = "Widely regarded by ancient schools and modern neuroscience as the most foundational practice for mental resilience. It trains the brain to notice warmth, opportunities, and simple everyday privileges.",
            fields = listOf(
                ProtocolField(
                    key = "grat_simple",
                    prompt = "What single micro-moment, clean comfort, or simple joy are you grateful for today?",
                    helperText = "Examples: hot water, a quiet coffee, cold clean air, a child's laugh, or a deep undisturbed breath."
                ),
                ProtocolField(
                    key = "grat_person",
                    prompt = "Who is one person you are grateful for today, and why?",
                    helperText = "Think of a quick kind interaction, or simply their existence and influence in your world."
                ),
                ProtocolField(
                    key = "grat_challenge",
                    prompt = "What struggle, boundary, or discomfort did you face today that you are thankful to have met?",
                    helperText = "Reframe a challenge: what core virtue (patience, resolve, empathy) was strengthened by this obstacle?"
                )
            ),
            iconName = "Favorite",
            themeColorName = "Rose"
        )

        val STOIC_DECISION = ProtocolDefinition(
            key = "STOIC_DECISION",
            title = "Consequential Decision",
            tagline = "Audit the choices that shape your trajectory",
            description = "Valued by the Stoics as a daily audit of action. By documenting your most consequential choice today, you highlight your active agency, trade-offs, and alignment with wisdom.",
            fields = listOf(
                ProtocolField(
                    key = "dec_what",
                    prompt = "What was the single most consequential decision or fork in the road you encountered today?",
                    helperText = "Identify a clear choice: a conversation started, an urge resisted, or a task prioritized."
                ),
                ProtocolField(
                    key = "dec_alts",
                    prompt = "What alternate options or pathways did you actively weigh?",
                    helperText = "Recognize the path not taken. What impulses or conveniences did you overcome to proceed?"
                ),
                ProtocolField(
                    key = "dec_reason",
                    prompt = "Why did you make this choice? Was it guided by courage, temperance, wisdom, or justice?",
                    helperText = "Dissect your core rationale. Was it reactionary/autopilot, or did you act out of noble character?"
                ),
                ProtocolField(
                    key = "dec_lesson",
                    prompt = "Fast-forwarding this decision, what is the expected outcome, and what did you learn?",
                    helperText = "Project the consequences forward. What does this decision teach you about yourself and future events?"
                )
            ),
            iconName = "Directions",
            themeColorName = "Emerald"
        )

        val DAILY_INTENTION = ProtocolDefinition(
            key = "DAILY_INTENTION",
            title = "Dichotomy of Control",
            tagline = "Separate your agency from external noise",
            description = "Epictetus' classic Stoic protocol. Separate the things that are 100% in your power from external circumstances, and examine how you governed your attitude toward both.",
            fields = listOf(
                ProtocolField(
                    key = "cont_intent",
                    prompt = "What was your central standard of conduct or objective today, and how did you hold to it?",
                    helperText = "What was your conscious intent? Did you lose yourself to reactive autopilot?"
                ),
                ProtocolField(
                    key = "cont_ext",
                    prompt = "What external events, remarks, or delays tested you today? Did you accept them gracefully?",
                    helperText = "Identify events completely outside your power. Did you waste agency fighting what is?"
                ),
                ProtocolField(
                    key = "cont_int",
                    prompt = "What action, response, or internal posture today was 100% in your control, and did you excel in it?",
                    helperText = "The core of Stoic character. Reflect on where you fully owned your response."
                )
            ),
            iconName = "Balance",
            themeColorName = "Amber"
        )

        val FEAR_SETTING = ProtocolDefinition(
            key = "FEAR_SETTING",
            title = "Stoic Fear Setting",
            tagline = "Disarm anxieties by structuring prevention and repair",
            description = "The preemptive fear mitigation protocol (Premeditatio Malorum). Translates vague, paralyzed anxieties into clear coordinates of actionable prevention and recovery plans.",
            fields = listOf(
                ProtocolField(
                    key = "fear_define",
                    prompt = "What specific worry, difficult action, or decision is causing internal friction right now?",
                    helperText = "Deconstruct the anxiety: state the nightmare scenario of what you are putting off or fearing."
                ),
                ProtocolField(
                    key = "fear_prevent",
                    prompt = "What explicit actions can you take to prevent or decrease the likelihood of this occurring?",
                    helperText = "Establish active guardrails. What micro-steps can protect this boundary?"
                ),
                ProtocolField(
                    key = "fear_repair",
                    prompt = "If the worst scenario happens, what can you do to repair, recover, or seek guidance?",
                    helperText = "Disarm the terror. Realize you are anti-fragile. Who could you call? How could you rebuild?"
                )
            ),
            iconName = "Security",
            themeColorName = "Indigo"
        )

        val ALL_PROTOCOLS = listOf(GRATITUDE, STOIC_DECISION, DAILY_INTENTION, FEAR_SETTING)

        fun getByKey(key: String): ProtocolDefinition {
            return ALL_PROTOCOLS.find { it.key == key } ?: GRATITUDE
        }

        /**
         * Helper to parse prompt-response map stored as a flat JSON string.
         */
        fun parseResponses(jsonStr: String): Map<String, String> {
            val map = mutableMapOf<String, String>()
            if (jsonStr.isEmpty()) return map
            try {
                val json = JSONObject(jsonStr)
                val keys = json.keys()
                while (keys.hasNext()) {
                    val k = keys.next()
                    map[k] = json.optString(k, "")
                }
            } catch (e: Exception) {
                // Return empty map on failure
            }
            return map
        }

        /**
         * Helper to serialize prompt-response map into a flat JSON string.
         */
        fun serializeResponses(map: Map<String, String>): String {
            val json = JSONObject()
            for ((k, v) in map) {
                json.put(k, v)
            }
            return json.toString()
        }
    }
}

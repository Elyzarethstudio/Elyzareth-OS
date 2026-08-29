package com.example.model

enum class LyricStudioMode {
    SIMPLE,
    ADVANCED
}

enum class AdvancedLyricTab {
    LYRICS,
    STYLES,
    AUDIO,
    MORE_OPTIONS
}

enum class AudioSourceType(val label: String, val iconDescription: String) {
    NONE("None", "No audio attached"),
    BROWSE("Browse Audio", "Selected from Audio Library"),
    UPLOAD("Uploaded File", "Uploaded local audio/stems"),
    RECORD("Recorded Clip", "Live voice/acoustic recording")
}

enum class VocalGender(val label: String) {
    ANY("Any"),
    MALE("Male"),
    FEMALE("Female")
}

data class AttachedAudio(
    val type: AudioSourceType,
    val title: String,
    val durationSeconds: Int = 30,
    val timestamp: Long = System.currentTimeMillis()
)

enum class VoiceSourceType(val label: String, val description: String) {
    NONE("None", "No voice attached"),
    RECORD("Record Voice", "Record real-time vocal timbre & pitch range"),
    UPLOAD("Upload Voice", "Upload isolated acapella / vocal stem"),
    LIBRARY("Voice Library", "Select from curated vocal personas")
}

data class AttachedVoice(
    val type: VoiceSourceType,
    val personaName: String,
    val timbre: String = "Warm & Resonant",
    val pitchRange: String = "Alto / Tenor",
    val timestamp: Long = System.currentTimeMillis()
)

data class AudioAcousticEvidence(
    val bpm: Int,
    val tempoDescription: String,
    val rhythmicFeel: String,
    val keySignature: String,
    val energyLevel: Float, // 0.0 to 1.0
    val texturalSignature: String,
    val instrumentalProfile: String
)

data class LyricEvidence(
    val theme: String,
    val narrativeArc: String,
    val emotionalProfile: String,
    val witnessObjects: List<String>,
    val temporalContext: String,
    val energyProfile: String,
    val languageCharacteristics: String,
    val creativeSignals: List<String>,
    val suggestedSonicVocabulary: List<String> = emptyList()
)

enum class MagicOperationType(val title: String, val description: String) {
    CREATE("Create", "Synthesize full song suite from concept"),
    REWRITE("Rewrite", "Polish & transform phrasing while keeping core meaning"),
    EXPAND("Expand", "Develop themes and add complementary stanzas"),
    CURE("G6 Cure", "Isolate salvageable gems from contaminated text and reconstruct"),
    STRUCTURE("Structure", "Architect verses, pre-chorus, chorus, bridge, and outro"),
    RHYME_METER("Rhyme & Meter", "Enforce strict syllable cadence and rhyme scheme"),
    STYLE_TRANSFORM("Style Transform", "Derive acoustic style vocabulary from lyrical evidence"),
    AUDIO_ALIGN("Audio Align", "Extract acoustic BPM/Key evidence and align meter")
}

data class Stanza(
    val id: String,
    val type: String, // Verse 1, Pre-Chorus, Chorus, Verse 2, Bridge, Outro
    val lines: List<String>,
    val syllableCounts: List<Int>,
    val rhymeScore: Float = 0.94f,
    val g1Status: VerificationState = VerificationState.VERIFIED,
    val isGemFlagged: Boolean = false
)

data class GeneratedSong(
    val id: String,
    val title: String,
    val genreTheme: String,
    val cadence: String,
    val rhymeScheme: String,
    val stanzas: List<Stanza>,
    val g3SealHash: String,
    val rawLyricText: String = "",
    val stylePrompt: String = "",
    val tempoBpm: Int = 120,
    val timeSignature: String = "4/4",
    val vocalTimbre: String = "Ethereal / Resonant",
    val acousticConstraint: ElyzarethAcousticConstraint? = ELYZARETH_RUSTIC_ACOUSTIC_v1,
    val sparseArrangementConstraints: SparseArrangementConstraints? = ELYZARETH_SPARSE_ARRANGEMENT_CONSTRAINTS_v1_0,
    val physicalAnchorDiagnostic: PhysicalAnchorDiagnosticResult? = null,
    val createdTimeMillis: Long = System.currentTimeMillis()
)

data class RhymeSuggestion(
    val word: String,
    val rhymes: List<String>,
    val syllable: Int,
    val score: Float
)

data class AudioCadenceProfile(
    val bpm: Int = 120,
    val timeSignature: String = "4/4",
    val targetSyllablesPerBar: Int = 8,
    val stressPattern: String = "DA-dum-DA-dum",
    val harmonicKey: String = "D Minor"
)


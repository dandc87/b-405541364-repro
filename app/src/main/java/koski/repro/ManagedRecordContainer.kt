package koski.repro

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.widthIn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@Stable
class ManagedRecordState {
    val records = mutableStateListOf<ManagedRecord>()

    internal fun animateToDismiss(record: ManagedRecord) {
        record.state.hide()
        if (!record.state.isVisible) {
            record.onDismissRequest()
        }
    }

    internal fun settleToDismiss(record: ManagedRecord, velocity: Float) {
        record.state.settle()
        if (!record.state.isVisible) {
            record.onDismissRequest()
        }
    }
}

@OptIn(ExperimentalUuidApi::class)
class ManagedRecord(
    val state: RecordState,
    val content: @Composable BoxScope.() -> Unit,
    internal val onDismissRequest: () -> Unit,
    val id: String = Uuid.random().toString(),
) {
    override fun equals(other: Any?): Boolean {
        return other is ManagedRecord && id == other.id
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }
}

private val LocalManagedRecordState = staticCompositionLocalOf<ManagedRecordState> {
    error("not provided")
}

@Composable
fun ManagedSheet(
    onDismissRequest: () -> Unit,
    content: @Composable () -> Unit,
) {
    val currentDismiss = rememberUpdatedState(onDismissRequest)
    val currentContent = rememberUpdatedState(content)
    val state = LocalManagedRecordState.current
    DisposableEffect(state) {
        val record = ManagedRecord(
            state = RecordState(),
            onDismissRequest = { currentDismiss.value() },
            content = { currentContent.value() },
        )
        state.records.add(record)
        onDispose {
            state.records.remove(record)
        }
    }
}

@Composable
fun ManagedRecordContainer(
    modifier: Modifier = Modifier,
    state: ManagedRecordState = remember { ManagedRecordState() },
    content: @Composable () -> Unit,
) {
    CompositionLocalProvider(LocalManagedRecordState provides state) {
        Box(
            modifier = modifier,
            propagateMinConstraints = true,
        ) {
            content()

            state.records.forEach { record ->
                key(record.id) {
                    if (record.state.ready) {
                        LaunchedEffect(record.state) {
                            record.state.show()
                        }
                    }
                    RecordLayout(
                        modifier = Modifier.pointerInput(
                            key1 = Unit,
                            block = {
                                detectTapGestures {
                                    state.animateToDismiss(record)
                                }
                            },
                        ),
                        sheetState = record.state,
                        settleToDismiss = { state.settleToDismiss(record, it) },
                        content = record.content,
                    )
                }
            }
        }
    }
}

@Stable
class RecordState {

    var ready: Boolean by mutableStateOf(false)

    var isVisible: Boolean by mutableStateOf(false)
        private set

    fun show() { isVisible = true }

    fun hide() { isVisible = false }

    fun settle() { isVisible = false }
}

@Composable
private fun RecordLayout(
    sheetState: RecordState,
    settleToDismiss: (velocity: Float) -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit,
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        SideEffect {
            println("fake usage for $settleToDismiss")
            sheetState.ready = true
        }
        Box(
            modifier = Modifier
                .widthIn(max = 480.dp)
                .fillMaxWidth(),
            propagateMinConstraints = true,
            content = content,
        )
    }
}

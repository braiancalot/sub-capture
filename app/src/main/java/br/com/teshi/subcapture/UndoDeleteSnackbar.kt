package br.com.teshi.subcapture

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

@Composable
fun UndoDeleteSnackbarHost(hostState: SnackbarHostState, modifier: Modifier) {
    SnackbarHost(hostState, modifier) { snackbarData ->
        Snackbar(
            snackbarData,
            shape = RoundedCornerShape(14.dp),
            containerColor = RaisedSurface,
            contentColor = PrimaryText,
            actionColor = AccentLavender,
        )
    }
}

@Composable
fun rememberUndoableDelete(
    hostState: SnackbarHostState,
    onDelete: (Int) -> CapturedSentence,
    onRestore: (CapturedSentence, Int) -> Unit,
): (Int) -> Unit {
    val snackbarScope = rememberCoroutineScope()
    val deletedMessage = stringResource(R.string.sentence_deleted)
    val undoLabel = stringResource(R.string.undo)
    return { index ->
        val deleted = onDelete(index)
        snackbarScope.launch {
            // A newer delete takes over the snackbar: only the last one can be undone
            hostState.currentSnackbarData?.dismiss()
            val result = hostState.showSnackbar(deletedMessage, undoLabel, duration = SnackbarDuration.Short)
            if (result == SnackbarResult.ActionPerformed) onRestore(deleted, index)
        }
    }
}

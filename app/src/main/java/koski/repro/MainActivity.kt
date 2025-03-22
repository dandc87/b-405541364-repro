package koski.repro

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MaterialTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    ManagedRecordContainer(modifier = Modifier.fillMaxSize()) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(innerPadding),
                            contentAlignment = Alignment.Center
                        ) {
                            var show by remember { mutableStateOf(false) }
                            Button(onClick = { show = true }) {
                                Text("Click to Crash")
                            }

                            if (show) {
                                ManagedSheet(onDismissRequest = { show = false }) {
                                    Surface {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(360.dp)
                                                .padding(16.dp),
                                            contentAlignment = Alignment.Center,
                                        ) {
                                            Text("If you see this, it did not crash")
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

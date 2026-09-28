package consumer

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import com.thanhng224.androidcomposebase.core.ui.base.BaseComposeActivity
import com.thanhng224.androidcomposebase.core.ui.theme.AndroidComposeBaseTheme

class ComposeConsumerActivity : BaseComposeActivity() {
    @Composable
    override fun Content() {
        AndroidComposeBaseTheme {
            Text(
                text = "Consumer Compose OK",
                style = MaterialTheme.typography.bodyLarge,
            )
        }
    }
}

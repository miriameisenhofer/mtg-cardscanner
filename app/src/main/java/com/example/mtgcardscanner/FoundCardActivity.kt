package com.example.mtgcardscanner

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.mtgcardscanner.ui.theme.MTGCardScannerTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.TextField
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import coil.compose.rememberImagePainter
import androidx.compose.runtime.remember
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType


class FoundCardActivity : ComponentActivity() {

    override fun onDestroy() {
        IMAGE_ANALYSIS_ENABLED = true
        FOUNDCARDACTIVITY_ENABLED = true
        super.onDestroy()
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val uriStringList = intent.getStringArrayListExtra("uriList")
        val uriList = uriStringList!!.map { Uri.parse(it)}

        val setStringList = intent.getStringArrayListExtra("setList")!!
        var pr = ""
        for (s in setStringList) {
            pr += s
        }
        Toast.makeText(baseContext, pr, Toast.LENGTH_SHORT).show()

        val card = intent.getParcelableExtra<ScryfallCard>("card")!!

        setContent {
            MTGCardScannerTheme {
                PagerView(uriList, setStringList, card)
            }
        }
    }
}

fun addToCollection(card: ScryfallCard, context: Context, amount: Int) {
    if (isInCSV(card)) {
        Toast.makeText(context, "TODO 1\namount = $amount", Toast.LENGTH_SHORT).show()
        increaseCardInCSV(card)
    } else {
        Toast.makeText(context, "TODO 2\namount = $amount", Toast.LENGTH_SHORT).show()
        addNewCardToCsv(card)
    }
}

fun isInCSV(card: ScryfallCard) : Boolean {
    // TODO
    return false
}

fun increaseCardInCSV(card: ScryfallCard) {
    //TODO
}

fun addNewCardToCsv(card: ScryfallCard) {
    //TODO
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PagerView(uriList: List<Uri>, setList: List<String>, card: ScryfallCard) {
    val pageCount = uriList.size

    val pagerState = rememberPagerState {
        pageCount
    }

    Column (verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .height(400.dp),
                //.background(color = Color.White),
            pageSpacing = 20.dp,
            contentPadding = PaddingValues(horizontal = 50.dp)
        ) { page ->
            //BannerItem(image = list[page])
            UriItem(image = uriList[page])
        }
        Column (verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
            val amountState = remember { AmountState()}
            Row (verticalAlignment = Alignment.CenterVertically) {
                Text(text = "${pagerState.currentPage}", color = Color.Blue)
                NumberField(amountState)
            }
            val context = LocalContext.current
            AddToCollectionButton(pagerState.currentPage) {
                addToCollection(card, context, amountState.amount.toInt())
            }
        }
    }
}

@Composable
fun AddToCollectionButton(index: Int, onClick: () -> Unit) {
    Button(onClick = { onClick() }) {
        Text("Add to Collection")
    }
}

class AmountState() {
    var amount: String by mutableStateOf("")
}
@Composable
fun NumberField(amountState: AmountState = remember { AmountState() }) {
    //var number by remember { mutableStateOf("") }
    TextField(
        value = amountState.amount,
        onValueChange = { amountState.amount = it},
        label = { Text("Enter Amount")},
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
    )
}

@OptIn(coil.annotation.ExperimentalCoilApi::class)
@Composable
fun UriItem(image: Uri) {
    Box(contentAlignment = Alignment.Center,
        modifier = Modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(10.dp))
    ) {
        Image(
            painter = rememberImagePainter(image),
            contentDescription = "Card Image",
        )
    }
}



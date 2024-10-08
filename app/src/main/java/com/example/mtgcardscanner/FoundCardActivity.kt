package com.example.mtgcardscanner

import android.content.Context
import android.net.Uri
import android.os.Build
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
import java.io.File
import kotlin.math.log


class FoundCardActivity : ComponentActivity() {

    companion object {
        val TAG = FoundCardActivity::class.java.name
    }

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

        val card = intent.getParcelableExtra<ScryfallCard>("card")!!

        setContent {
            MTGCardScannerTheme {
                PagerView(uriList, setStringList, card)
            }
        }
    }
}

fun addToCollection(card: ScryfallCard, setName: String, context: Context, amount: Int) {
    if (isInCSV(card, context)) {
        increaseCardInCSV(card, setName, amount, context)
    } else {
        addNewCardToCsv(card, setName, amount, context)
    }
}

fun isInCSV(card: ScryfallCard, context: Context) : Boolean {
    val inputStream = context.contentResolver.openInputStream(COLLECTION_FILE!!)
    val reader = inputStream?.bufferedReader()
    val lines = reader?.lineSequence()
    for (l in lines!!) {
        val lList = l.split(";")
        if (lList[0] == card.name) {
            inputStream.close()
            return true
        }
    }
    inputStream.close()
    return false
}

fun increaseCardInCSV(card: ScryfallCard, setName: String, amount: Int, context: Context) {
    var txt = ""
    val inputStream = context.contentResolver.openInputStream(COLLECTION_FILE!!)
    val reader = inputStream?.bufferedReader()
    val lines = reader?.lineSequence()
    for (l in lines!!) {
        val lList = l.split(";")
        if (lList[0] == card.name) {
            // Adjust line in which card is found
            txt += card.name + ";" + (lList[1].toInt() + amount) + ";" + card.manaCost
            // Search for column which refers to set in question
            var containedSet = false
            for (i in 3..< lList.size) {
                if (lList[i] == setName) {
                    containedSet = true
                    txt += ";" + setName + ";" + (lList[i + 1].toInt() + amount)
                    for (remainingSetEntry in lList.slice(i+2..<lList.size)){
                        txt += ";$remainingSetEntry"
                    }
                    break
                } else {
                    txt += ";" + lList[i]
                }
            }
            if (!containedSet) {
                txt += ";$setName;$amount"
            }
            txt += "\n"
        } else {
            // Keep line the same, as it is different card
            txt += l + "\n"
        }
    }
    inputStream.close()

    try {
        context.contentResolver.openOutputStream(COLLECTION_FILE!!,"w")?.use {outputStream ->
            val writer = outputStream.bufferedWriter()
            writer.write(txt)
            writer.flush()
        }
    } catch (e: Exception) {
        Log.e(FoundCardActivity.TAG,"Failed to write to .csv file")
    }
}

fun addNewCardToCsv(card: ScryfallCard, setName: String, amount: Int, context: Context) {
    val txt = card.name + ";" + amount + ";" + card.manaCost + ";" + setName + ";" + amount
    try {
        context.contentResolver.openOutputStream(COLLECTION_FILE!!, "wa")?.use { outputStream ->
            val writer = outputStream.bufferedWriter()
            writer.write(txt)
            writer.newLine()
            writer.flush()
            outputStream.close()
        }
    } catch (e: Exception) {
        Log.e(FoundCardActivity.TAG, "Failed to write to .csv file")
    }
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
                addToCollection(card, setList[pagerState.currentPage], context, amountState.amount.toInt())
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



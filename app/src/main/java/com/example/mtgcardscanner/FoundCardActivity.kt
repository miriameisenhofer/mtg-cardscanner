package com.example.mtgcardscanner

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

        Toast.makeText(baseContext, "FCA", Toast.LENGTH_SHORT).show()
        val uriStringList = intent.getStringArrayListExtra("uriList")
        val uriList = uriStringList!!.map { Uri.parse(it)}
        uriList.forEach { uri ->
            Toast.makeText(baseContext, "FCA, uri: $uri", Toast.LENGTH_SHORT).show()
        }
        Toast.makeText(baseContext, "Done uris", Toast.LENGTH_SHORT).show()
        setContent {
            MTGCardScannerTheme {
                PagerView(uriList)
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PagerView(uriList: List<Uri>) {
    /*val list = listOf(
        R.drawable.c16_143_burgeoning,
        R.drawable.c16_143_burgeoning
    )*/
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
            Row (verticalAlignment = Alignment.CenterVertically) {
                Text(text = "${pagerState.currentPage}", color = Color.Blue)
                NumberField()
            }
            val context = LocalContext.current
            AddToCollectionButton(pagerState.currentPage) {
                Toast.makeText(
                    context,
                    "TODO",
                    Toast.LENGTH_SHORT
                ).show()
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

@Composable
fun NumberField() {
    var number by remember { mutableStateOf("") }
    TextField(
        value = number,
        onValueChange = { number = it},
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
@Composable
fun BannerItem(image: Int) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(10.dp))
    ) {
        Image(
            painter = painterResource(id = image),
            contentDescription = "Card Image"
        )
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    MTGCardScannerTheme {
        Text(text = "a")
    }
}
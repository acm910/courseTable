package com.example.coursetable


import androidx.compose.foundation.gestures.ScrollableState
import androidx.compose.foundation.gestures.rememberScrollableState
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape

import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.nio.file.WatchEvent


@Composable
@Preview
fun weekCourse(){
    var scrollState = rememberScrollState()
    Row(
        modifier = Modifier.horizontalScroll(scrollState)
    ){
        for(i in 0..6)
            dayCourse()
    }
}
@Composable
@Preview
fun dayCourse(){

    Column(
        modifier = Modifier.padding(5.dp).size(70.dp,700.dp)
    ) {
        Text(text = "12",fontSize = 15.sp, textAlign = TextAlign.Center, modifier = Modifier.padding(end = 5.dp).fillMaxWidth())
        testPage2(color = Color(0x40ff0000),height = 100,width = 65)
        testPage2(color = Color(0x60ffff00),height = 150,width = 65)
        testPage2(color = Color(0x30ff00ff),height = 150,width = 65)
        testPage2(color = Color(0x6000ff00),height = 100,width = 65)
        testPage2(color = Color(0x300000ff),height = 150,width = 65)

    }


}

@Composable
fun testPage(name : String){
    Text(text = "Hello $name!", modifier = Modifier.padding(16.dp).size(100.dp,40.dp), color = Color.Red)
}

@Composable

fun testPage2(color:Color,height:Int,width:Int){
    Surface(color = color,
        modifier = Modifier.padding(bottom =5.dp,top = 5.dp).size(width.dp,height.dp),
        shape = RoundedCornerShape(8.dp)
    ) {

    }
}
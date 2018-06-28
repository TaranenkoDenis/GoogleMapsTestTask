package serg.denis.taranenko.googlemapstesttask

import android.graphics.Color
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.AutoCompleteTextView
import android.widget.ArrayAdapter



class TestAutofillActivity : AppCompatActivity() {

    var fruits = arrayOf("Apple", "Banana", "Cherry", "Date", "Grape", "Kiwi", "Mango", "Pear")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.text_autocomplete_example)

        //Creating the instance of ArrayAdapter containing list of fruit names
        val adapter = ArrayAdapter(this, android.R.layout.select_dialog_item, fruits)
        //Getting the instance of AutoCompleteTextView
        val actv = findViewById<View>(R.id.autoCompleteTextView) as AutoCompleteTextView
        actv.threshold = 1//will start working from first character
        actv.setAdapter(adapter)//setting the adapter data into the AutoCompleteTextView
        actv.setTextColor(Color.RED)
    }
}

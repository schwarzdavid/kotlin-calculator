package rocks.schwarzdavid.calculator

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import rocks.schwarzdavid.calculator.helper.MathParser

const val STATE_INPUT_CONTENT = "inputContent"

class MainActivity : AppCompatActivity() {

    private var _inputTextView: TextView? = null;
    private var _previewTextView: TextView? = null;

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        _inputTextView = findViewById<TextView>(R.id.inputText);
        _previewTextView = findViewById<TextView>(R.id.previewText);
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putString(STATE_INPUT_CONTENT, _inputTextView?.getText().toString());

        super.onSaveInstanceState(outState)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        _inputTextView?.text = savedInstanceState.getString(STATE_INPUT_CONTENT);

        super.onRestoreInstanceState(savedInstanceState)
    }

    fun findViewsByTag(root: ViewGroup, tag: String): ArrayList<View> {
        val views = arrayListOf<View>();
        // TODO: https://stackoverflow.com/a/8831593
    }

    fun onInputButtonClick(view: View) {
        val text = (view as Button).text;
        if (_inputTextView?.text === getString(R.string.default_input)) {
            _inputTextView?.setText(text);
        } else {
            _inputTextView?.append(text);
        }
        _inputTextView?.setAutoSizeTextTypeWithDefaults(TextView.AUTO_SIZE_TEXT_TYPE_UNIFORM);
    }
}

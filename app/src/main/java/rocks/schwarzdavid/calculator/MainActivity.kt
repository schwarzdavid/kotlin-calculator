package rocks.schwarzdavid.calculator

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import kotlinx.android.synthetic.main.activity_main.*
import io.kaen.dagger.ExpressionParser
import java.lang.Exception

const val STATE_INPUT_CONTENT = "inputContent"

class MainActivity : AppCompatActivity() {

    private val OPERATORS = setOf<Char>('*', '/', '+', '-');
    private var _openBrackets = 0;

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val rootControlGroup = findViewById<ViewGroup>(R.id.controls);

        for (numBtn in findViewsByTag(rootControlGroup, getString(R.string.type_number))) {
            numBtn.setOnClickListener {
                appendToExpression((it as Button).text);
            }
        }

        for (opBtn in findViewsByTag(rootControlGroup, getString(R.string.type_operator))) {
            opBtn.setOnClickListener {
                input_text.text = stripOperatorFromEnd(input_text.text);
                appendToExpression((it as Button).text);
            }
        }

        open_bracket.setOnClickListener {
            _openBrackets++;
            appendToExpression((it as Button).text);
        }

        close_bracket.setOnClickListener {
            if (_openBrackets > 0) {
                _openBrackets--;
                appendToExpression((it as Button).text);
            }
        }

        clear_one.setOnClickListener {
            input_text.text = input_text.text.dropLast(1);

            if (input_text.text.isEmpty()) {
                input_text.text = getString(R.string.default_result);
            }
        }

        clear_all.setOnClickListener {

        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putString(STATE_INPUT_CONTENT, input_text.getText().toString());

        super.onSaveInstanceState(outState)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        input_text.text = savedInstanceState.getString(STATE_INPUT_CONTENT);

        super.onRestoreInstanceState(savedInstanceState)
    }

    /**
     * @see https://stackoverflow.com/a/8831593
     */
    fun findViewsByTag(root: ViewGroup, tag: String): ArrayList<View> {
        val views = arrayListOf<View>();
        for (i in 0 until root.getChildCount()) {
            val child = root.getChildAt(i);
            if (child is ViewGroup) {
                views.addAll(findViewsByTag(child, tag));
            }

            val tagObj = child.getTag();
            if (tagObj !== null && tagObj.equals(tag)) {
                views.add(child);
            }
        }
        return views;
    }

    fun appendToExpression(txt: CharSequence) {
        input_text.append(txt);

        if (input_text.text.isEmpty()) {
            preview_text.setText(getString(R.string.default_result));
        } else {
            preview_text.setText(
                evaluateMathExpression(
                    stripOperatorFromEnd(input_text.text).toString() + ")".repeat(
                        _openBrackets
                    )
                ).toString()
            );
        }
    }

    fun stripOperatorFromEnd(expression: CharSequence): CharSequence {
        var out = expression;
        if (OPERATORS.contains(out.takeLast(1).last())) {
            out = out.dropLast(1);
        }
        return out;
    }

    fun evaluateMathExpression(expression: String): Double? {
        try {
            return ExpressionParser().evaluate(expression);
        } catch (e: Exception) {
            return null;
        }
    }
}

package rocks.schwarzdavid.calculator

import android.animation.AnimatorSet
import android.animation.ArgbEvaluator
import android.animation.ValueAnimator
import android.content.res.Resources
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import kotlinx.android.synthetic.main.activity_main.*
import io.kaen.dagger.ExpressionParser
import java.lang.Exception

const val STATE_INPUT_CONTENT = "inputContent";
const val STATE_PREVIEW_CONTENT = "previewContent";

val Float.sp: Int
    get() = TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_SP,
        this,
        Resources.getSystem().displayMetrics
    ).toInt();

class MainActivity : AppCompatActivity() {

    //**********************************************
    // PRIVATE PROPERTIES
    //**********************************************
    private val OPERATORS = setOf<Char>('*', '/', '+', '-');
    private var openBrackets = 0;
    private var isPreviewEnlarged = true;

    private val SMALL_TEXT_SIZE = 40f.sp;
    private val SMALL_TEXT_COLOR = Color.rgb(0x77, 0x77, 0x77);

    //**********************************************
    // LIFECYCLE METHODS
    //**********************************************
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val rootControlGroup = findViewById<ViewGroup>(R.id.controls);
        for (numBtn in findViewsByTag(rootControlGroup, getString(R.string.type_number))) {
            numBtn.setOnClickListener(_onNumberButtonClick());
        }
        for (opBtn in findViewsByTag(rootControlGroup, getString(R.string.type_operator))) {
            opBtn.setOnClickListener(_onOperatorButtonClick());
        }

        open_bracket.setOnClickListener(_onBracketOpenButtonClick());
        close_bracket.setOnClickListener(_onBracketCloseButtonClick());
        clear_one.setOnClickListener(_onClearOneButtonClick());
        clear_all.setOnClickListener(_onClearAllButtonClick());
        equals.setOnClickListener(_onEqualButtonClick());

        enlargePreviewText();
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putString(STATE_INPUT_CONTENT, input_text.getText().toString());
        outState.putString(STATE_PREVIEW_CONTENT, preview_text.getText().toString());

        super.onSaveInstanceState(outState)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        input_text.text = savedInstanceState.getString(STATE_INPUT_CONTENT);
        preview_text.text = savedInstanceState.getString(STATE_PREVIEW_CONTENT);

        super.onRestoreInstanceState(savedInstanceState)
    }

    //**********************************************
    // EVENT LISTENER
    //**********************************************
    fun _onNumberButtonClick(): View.OnClickListener {
        return View.OnClickListener {
            appendToExpression((it as Button).text);
        };
    }

    fun _onOperatorButtonClick(): View.OnClickListener {
        return View.OnClickListener {
            if (input_text.text.isEmpty()) {
                if (preview_text.text.startsWith('=')) {
                    input_text.text = preview_text.text.drop(2);
                } else {
                    input_text.text = preview_text.text;
                }
            }
            input_text.text = stripOperatorFromEnd(input_text.text);
            appendToExpression((it as Button).text);
        };
    }

    fun _onBracketOpenButtonClick(): View.OnClickListener {
        return View.OnClickListener {
            openBrackets++;
            appendToExpression((it as Button).text);
        };
    }

    fun _onBracketCloseButtonClick(): View.OnClickListener {
        return View.OnClickListener {
            if (openBrackets > 0) {
                openBrackets--;
                appendToExpression((it as Button).text);
            }
        };
    }

    fun _onClearOneButtonClick(): View.OnClickListener {
        return View.OnClickListener {
            input_text.text = input_text.text.dropLast(1);

            if (input_text.text.isEmpty()) {
                preview_text.text = getString(R.string.default_result);
                enlargePreviewText();
            }
        };
    }

    fun _onClearAllButtonClick(): View.OnClickListener {
        return View.OnClickListener {
            input_text.text = "";
            preview_text.text = getString(R.string.default_result);
            enlargePreviewText();
        };
    }

    fun _onEqualButtonClick(): View.OnClickListener {
        return View.OnClickListener {
            if (input_text.text.isNotEmpty()) {
                preview_text.text = evaluateMathExpression(input_text.text.toString());
                input_text.text = "";

                val textColorAnimator = ValueAnimator.ofObject(
                    ArgbEvaluator(),
                    SMALL_TEXT_COLOR,
                    input_text.currentTextColor
                ).apply {
                    addUpdateListener {
                        preview_text.setTextColor(it.animatedValue as Int);
                    };
                };

                val textSizeAnimator = ValueAnimator.ofInt(
                    SMALL_TEXT_SIZE,
                    input_text.autoSizeMaxTextSize
                ).apply {
                    addUpdateListener {
                        preview_text.setAutoSizeTextTypeUniformWithConfiguration(
                            input_text.autoSizeMinTextSize,
                            it.animatedValue as Int,
                            input_text.autoSizeStepGranularity,
                            TypedValue.COMPLEX_UNIT_PX
                        );
                    }
                };

                AnimatorSet().apply {
                    playTogether(textColorAnimator, textSizeAnimator);
                    duration = 200;
                    start();
                };
                isPreviewEnlarged = true;
            }
        };
    }

    //**********************************************
    // HELPER METHODS
    //**********************************************
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
        if (isPreviewEnlarged) {
            resetPreviewTextAppearance();
        }
        input_text.append(txt);
        preview_text.setText(evaluateMathExpression(prepareExpression(input_text.text)));
    }

    fun prepareExpression(expression: CharSequence): String {
        return stripOperatorFromEnd(expression).toString() + ")".repeat(openBrackets);
    }

    fun stripOperatorFromEnd(expression: CharSequence): CharSequence {
        var out = expression;
        if (OPERATORS.contains(out.takeLast(1).last())) {
            out = out.dropLast(1);
        }
        return out;
    }

    fun evaluateMathExpression(expression: String): String {
        try {
            val resultValue = ExpressionParser().evaluate(expression);
            val resultStr: String;
            if (resultValue % 1 == 0.0) {
                resultStr = resultValue.toInt().toString();
            } else {
                resultStr = resultValue.toString();
            }
            return "= " + resultStr;
        } catch (e: Exception) {
            return "Fehler";
        }
    }

    fun enlargePreviewText() {
        preview_text.setAutoSizeTextTypeUniformWithConfiguration(
            input_text.autoSizeMinTextSize,
            input_text.autoSizeMaxTextSize,
            input_text.autoSizeStepGranularity,
            TypedValue.COMPLEX_UNIT_PX
        );
        preview_text.setTextColor(input_text.currentTextColor);
        isPreviewEnlarged = true;
    }

    fun resetPreviewTextAppearance() {
        preview_text.setAutoSizeTextTypeUniformWithConfiguration(
            input_text.autoSizeMinTextSize,
            SMALL_TEXT_SIZE,
            input_text.autoSizeStepGranularity,
            TypedValue.COMPLEX_UNIT_PX
        );
        preview_text.setTextColor(SMALL_TEXT_COLOR);
        isPreviewEnlarged = false;
    }
}

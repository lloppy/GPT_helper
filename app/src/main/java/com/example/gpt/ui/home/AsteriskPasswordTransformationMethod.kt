import android.text.method.PasswordTransformationMethod
import android.view.View

class AsteriskPasswordTransformationMethod : PasswordTransformationMethod() {
    override fun getTransformation(source: CharSequence?, view: View?): CharSequence {
        return if (source != null) {
            AsteriskCharSequence(source)
        } else {
            super.getTransformation(source, view)
        }
    }

    private inner class AsteriskCharSequence(private val source: CharSequence) : CharSequence {
        override val length: Int
            get() = source.length

        override fun get(index: Int): Char {
            return if (index >= source.length - 4) {
                source[index]
            } else {
                if(index%5 == 0)'*'
                else ' '

            }
        }

        override fun subSequence(startIndex: Int, endIndex: Int): CharSequence {
            return if (startIndex >= source.length - 4) {
                source.subSequence(startIndex, endIndex)
            } else {
                AsteriskCharSequence(source.subSequence(startIndex, endIndex))
            }
        }
    }
}

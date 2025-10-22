package site.addzero.valid.valid_ex


import site.addzero.util.ThreadLocalUtil
import javax.validation.ConstraintValidator
import javax.validation.ConstraintValidatorContext

class ThisValidator : ConstraintValidator<ThisValid?, Any?> {
    override fun isValid(p0: Any?, p1: ConstraintValidatorContext?): Boolean {
        ThreadLocalUtil.set(p0)
        return true
    }
}

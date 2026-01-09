package site.addzero.aop.dicttrans.inter

interface TPredicate {
  fun tBlackList(): List<Class<out Any>>
}

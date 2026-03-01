package site.addzero.lsi.environment

import site.addzero.lsi.logger.LsiLogger

interface LsiEnvironment{
     val options: MutableMap<String, String>?
    /**
     * Returns the messager used to report errors, warnings, and other
     * notices.
     *
     * @return the messager
     */
     val logger: LsiLogger?

}

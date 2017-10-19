import javax.inject.Inject

import play.api.http.HttpFilters
import play.api.mvc.EssentialFilter

class Filters @Inject() () extends HttpFilters {
  def filters: Seq[EssentialFilter] = Seq()
}

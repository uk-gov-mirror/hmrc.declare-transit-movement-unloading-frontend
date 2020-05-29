package services.mocks
import org.mockito.Mockito.{reset, when}
import org.scalatest.{BeforeAndAfterEach, Suite}
import org.scalatestplus.mockito.MockitoSugar
import services.DateTimeService

trait MockDateTimeService extends MockitoSugar with BeforeAndAfterEach {
  this: Suite =>

  val mockTimeService: DateTimeService = mock[DateTimeService]

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockTimeService)
  }

  def mockDateFormatted(response: String): Unit = {
    when(mockTimeService.dateFormatted)
      .thenReturn(response)
  }

}
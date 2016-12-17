package com.example.app.models

import java.time.{DayOfWeek, LocalDate, LocalDateTime, LocalTime}
import java.util

import com.github.wslotkin.itinerator.generator.{ActivityProvider, ItineraryGenerator, ItineraryGeneratorRunner}
import com.github.wslotkin.itinerator.generator.config.{ImmutableGeneticAlgorithmConfig, ImmutableOptimizationConfig, ItineratorConfig => JavaItineratorConfig}
import com.github.wslotkin.itinerator.generator.datamodel.{ImmutableLocation, Activity => JavaActivity, ActivityType => JavaActivityType, WeeklySchedule => JavaWeeklySchedule, WeeklyShift => JavaWeeklyShift, WeeklyTimePoint => JavaWeeklyTimePoint}
import org.joda.time.DateTime
import org.slf4j.LoggerFactory

import scala.collection.JavaConversions._
import scala.concurrent.Await
import scala.concurrent.duration.Duration


class ItineraryGeneration(itinerary: Itinerary) {

  val logger = LoggerFactory.getLogger(getClass)

  def createItinerary() = {

    val start = itinerary.startDateTime
    val end = itinerary.endDateTime
    val excludedActivityIds = Await.result(
          RejectedActivity.byItineraryId(itinerary.id),
          Duration.fromNanos(10000000000L)
        ).map(_.activityId).toSet

    lazy val optimizerConfig = ImmutableOptimizationConfig.builder()
      .geneticAlgorithmConfig(
        ImmutableGeneticAlgorithmConfig.builder()
          .populationSize(20)
          .build()
      )
      .itineratorConfig(new ItineratorConfig(start, end, excludedActivityIds))
      .build()

    val generator = ItineraryGeneratorRunner.createOptimizer(optimizerConfig, activityProvider)

    val result = generator.run()

    val newItinerary = result.getItinerary()

    def scheduledActivityIdFixer(str: String) =
      try{
        str.toInt
      } catch { case _ => 0}

    newItinerary.getEvents.toSeq.map(e => {
      val activity = e.getActivity
      val time = e.getEventTime

      val start = time.getStart
      val end = time.getEnd

      //AN IMPORTANT HOOK IN FOR TIME ZONES

      val newStart = new DateTime(start.getYear, start.getMonthValue, start.getDayOfMonth, start.getHour, start.getMinute)
      val newEnd = new DateTime(end.getYear, end.getMonthValue, end.getDayOfMonth, end.getHour, end.getMinute)

      ScheduledActivity(itinerary.id,
        scheduledActivityIdFixer(activity.getId),
        newStart,
        newEnd,
        false,
        0
      )
    })

  }

  lazy val activityProvider = new ActivityProvider {
    def doGetActivities() = {
      val scalaActivities = Await.result(City.availableActivitiesByCityId(itinerary.cityId), Duration.fromNanos(10000000000L))
      scalaActivities.map(a => new ActivityConversion(a))
    }
  }

  class ActivityConversion(activity: Activity) extends JavaActivity{
      def getId() =
        activity.id.toString

      def getDuration() =
        activity.duration.map(_.getStandardMinutes).getOrElse(60L)

      def getLocation() =
        ImmutableLocation.of(activity.location.latitude, activity.location.longitude)

      def getCost() =
        0.0

      def getScore() =
        activity.funRating

      def getType() =
        activity.activityType match {
          case ActivityType.activity => JavaActivityType.ACTIVITY
          case ActivityType.hotel => JavaActivityType.HOTEL
          case ActivityType.restaurant => JavaActivityType.FOOD
        }

      def getWeeklySchedule: WeeklySchedule = {
        val ranges = activity.weekOpeningHoursRanges.map(_.map(wo => {
          new WeeklyShift(wo.startHours, wo.startMinutes, wo.endHours, wo.endMinutes)

        })).getOrElse(Nil)

        new WeeklySchedule(ranges)
      }
  }

  class WeeklySchedule(shifts: Seq[WeeklyShift]) extends JavaWeeklySchedule(shifts)

  class WeeklyShift(startHours: Int, startMinutes: Int, endHours: Int, endMinutes: Int) extends JavaWeeklyShift{

    def getStartTime: WeeklyTimePoint =
      new WeeklyTimePoint(startHours, startMinutes)

    def getEndTime: WeeklyTimePoint =
      new WeeklyTimePoint(endHours, endMinutes)
  }

  class WeeklyTimePoint(hours: Int, minutes: Int) extends JavaWeeklyTimePoint {

    def getDayOfWeek: DayOfWeek =
      DayOfWeek.of(math.floor(hours / 24.0).toInt)

    def getTimeOfDay: LocalTime =
      LocalTime.of(hours % 24, minutes, 0, 0)
  }

  //THIS IS A VERY IMPORTANT HOOK IN FOR TIME ZONES
  class ItineratorConfig(startTime: DateTime, endTime: DateTime, excludedActivityIds: Set[Int]) extends JavaItineratorConfig {
    def getExcludedActivityIds: util.Set[String] = {
      val stringIds = excludedActivityIds.map(_.toString)
      stringIds.toSet[String]
    }

    def getStartTime() = {
      LocalDateTime.of(LocalDate.of(startTime.getYear, startTime.getMonthOfYear, startTime.getDayOfMonth),
      LocalTime.of(startTime.getHourOfDay, startTime.getMinuteOfHour, 0, 0))
    }

    def getEndTime() = {
      LocalDateTime.of(LocalDate.of(endTime.getYear, endTime.getMonthOfYear, endTime.getDayOfMonth),
      LocalTime.of(endTime.getHourOfDay, endTime.getMinuteOfHour, 0, 0))
    }

  }
}


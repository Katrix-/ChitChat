package net.katsstuff.chitchat.persistant

import java.nio.file.Path

import org.spongepowered.api.text.{Text, TextTemplate}
import org.spongepowered.api.text.format.TextColors._

import io.github.katrix.katlib.KatPlugin
import io.github.katrix.katlib.helper.Implicits._
import io.github.katrix.katlib.helper.LogHelper
import io.github.katrix.katlib.persistant.{CommentedConfigValue, ConfigLoader, ConfigValue}

class ChitChatConfigLoader(dir: Path)(implicit plugin: KatPlugin) extends ConfigLoader[ChitChatConfig](dir, identity) {
  override def loadData: ChitChatConfig = {
    val loaded = cfgRoot.getNode("version").getString("1") match {
      case "1" => new ChitChatConfigV1(cfgRoot, default)
      case unknown =>
        LogHelper.error(s"Unknown config version $unknown, using default instead")
        default
    }
    saveData(loaded)
    loaded
  }

  val default = new ChitChatConfig {

    override val version = ConfigValue("1", "Please don't touch this", Seq("version"))

    override val mentionPling = ConfigValue(true, "Should players be plinged if they are mentioned", Seq("chat", "mention-pling"))

    override val defaultPrefix = ConfigValue(Text.EMPTY, "Prefix to add if no prefix is found", Seq("formatting", "default", "prefix"))
    override val defaultSuffix = ConfigValue(Text.EMPTY, "Suffix to add if no suffix is found", Seq("formatting", "default", "suffix"))

    override val headerTemplate =
      ConfigValue(tt"$TemplatePrefix$TemplateHeader: ", "Template to use for the header", Seq("formatting", "chat", "header-template"))
    override def suffixTemplate = ConfigValue(tt"$TemplateSuffix", "Template to use for the suffix", Seq("formatting", "chat", "suffix-template"))

    override val joinTemplate =
      ConfigValue(
        tt"${GREEN}The player $AQUA${"body"}$GREEN has joined the server",
        "Template to use when someone joins",
        Seq("formatting", "connect", "join-template")
      )
    override val disconnectTemplate =
      ConfigValue(
        tt"${RED}The player $AQUA${"body"}$RED has left the server",
        "Template to use when someone disconnects",
        Seq("formatting", "connect", "disconnect-template")
      )

    override val announceTemplate: CommentedConfigValue[TextTemplate] =
      ConfigValue(tt"$BLUE[Announcement]$RESET $TemplateHeader: ", "The template for announcements", Seq("formatting", "cmd", "announce"))
    override val meTemplate: CommentedConfigValue[TextTemplate] =
      ConfigValue(tt"* $TemplateHeader ", "The template for the me cmd", Seq("formatting", "cmd", "me"))
    override val pmTemplate: CommentedConfigValue[TextTemplate] =
      ConfigValue(tt"From $Sender to $Receiver: ", "The template used for PMs", Seq("formatting", "cmd", "pm"))
    override val shoutTemplate : CommentedConfigValue[TextTemplate] =
      ConfigValue(tt"$TemplateHeader shouts: ", "The template used when shouting to a channel", Seq("formatting", "cmd", "shout"))
  }
}

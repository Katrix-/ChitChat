package net.katsstuff.chitchat.chat.channel

import scala.collection.mutable
import scala.ref.WeakReference
import scala.reflect.ClassTag
import scala.util.Try

import org.spongepowered.api.text.Text
import org.spongepowered.api.text.channel.MessageReceiver

import io.github.katrix.katlib.serializer.ConfigSerializerBase.{ConfigNode, ConfigSerializer}
import net.katsstuff.chitchat.chat.LocalizedMessageChannel

case class ExtraData(displayName: String, data: Any)

/**
  * Base trait for all channels.
  * All channels are required to have a name, a prefix, and a description.
  * How to represent them is up to the implementation.
  *
  * A channel also has a set of members,
  * which should be the members of the [[LocalizedMessageChannel]].
  */
trait Channel {
  type Self <: Channel

  def messageChannel: LocalizedMessageChannel

  def name:                    String
  def name_=(newName: String): Self

  def prefix:                    Text
  def prefix_=(newPrefix: Text): Self

  def description:                         Text
  def description_=(newDescription: Text): Self

  def members:                                                    Set[WeakReference[MessageReceiver]]
  def members_=(newMembers: Set[WeakReference[MessageReceiver]]): Self

  def addMember(receiver: MessageReceiver): Self =
    members = members + WeakReference(receiver)
  def removeMember(receiver: MessageReceiver): Self =
    members = members.filterNot(_.get.contains(receiver))

  /**
    * The display name of this type of channel.
    */
  def typeName: String

  /**
    * The extra data this channel holds.
    */
  def extraData: Map[String, ExtraData] = Map.empty

  /**
    * Handles changing custom data.
    * @param data The data to set. The implementation decides what is a
    *             correct format for the data.
    * @return Left with error message if setting the data failed, else Right.
    */
  def handleExtraData(data: String): Either[Text, Self]
}

object Channel {

  //Yuck!
  //We need to allow custom channel types that are provided at runtime. As such we can't use implicits to resolve the serializers.

  private val nameToSerializer = new mutable.HashMap[String, ConfigSerializer[_ <: Channel]]
  private val classToName      = new mutable.HashMap[Class[_ <: Channel], String]

  /**
    * Due to allowing the system to be expanded dynamically,
    * we need to be able to add new types to serialization and
    * deserialization at runtime. And thus this thing was born.
    * @param name The name of the channel type when serialized.
    * @tparam A The channel type
    */
  def registerChannelType[A <: Channel](name: String)(implicit tag: ClassTag[A], ser: ConfigSerializer[A]): Unit = {
    val clazz = tag.runtimeClass.asInstanceOf[Class[A]]

    nameToSerializer.put(name, ser)
    classToName.put(clazz, name)
  }

  implicit object ChannelSerializer extends ConfigSerializer[Channel] {
    //Super hacky
    override def write(obj: Channel, node: ConfigNode): ConfigNode = {
      val withType = node.getNode("type").write(classToName(obj.getClass)).getParent
      nameToSerializer(classToName(obj.getClass))
        .asInstanceOf[ConfigSerializer[obj.Self]]
        .write(obj.asInstanceOf[obj.Self], withType)
    }

    override def read(node: ConfigNode): Try[Channel] =
      node.getNode("type").read[String].flatMap(tpe => nameToSerializer(tpe).read(node))
  }
}

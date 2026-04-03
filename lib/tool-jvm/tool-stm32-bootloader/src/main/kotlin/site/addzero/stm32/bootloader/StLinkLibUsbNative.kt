package site.addzero.stm32.bootloader

import com.sun.jna.Library
import com.sun.jna.Native
import com.sun.jna.NativeLibrary
import com.sun.jna.Pointer
import com.sun.jna.PointerType
import com.sun.jna.Structure
import com.sun.jna.ptr.IntByReference
import com.sun.jna.ptr.PointerByReference

/**
 * libusb 极简 JNA 绑定。
 *
 * 这里只保留 ST-Link 通信会用到的那一小部分 API。
 */
internal interface LibUsbNative : Library {
    fun libusb_init(context: PointerByReference): Int

    fun libusb_exit(context: LibUsbContext?)

    fun libusb_get_device_list(
        context: LibUsbContext?,
        list: PointerByReference,
    ): Long

    fun libusb_free_device_list(
        list: Pointer,
        unrefDevices: Int,
    )

    fun libusb_get_device_descriptor(
        device: LibUsbDevice,
        descriptor: LibUsbDeviceDescriptor,
    ): Int

    fun libusb_get_active_config_descriptor(
        device: LibUsbDevice,
        config: PointerByReference,
    ): Int

    fun libusb_free_config_descriptor(config: Pointer)

    fun libusb_open(
        device: LibUsbDevice,
        handle: PointerByReference,
    ): Int

    fun libusb_close(handle: LibUsbDeviceHandle)

    fun libusb_get_configuration(
        handle: LibUsbDeviceHandle,
        configuration: IntByReference,
    ): Int

    fun libusb_set_configuration(
        handle: LibUsbDeviceHandle,
        configuration: Int,
    ): Int

    fun libusb_claim_interface(
        handle: LibUsbDeviceHandle,
        interfaceNumber: Int,
    ): Int

    fun libusb_clear_halt(
        handle: LibUsbDeviceHandle,
        endpoint: Byte,
    ): Int

    fun libusb_release_interface(
        handle: LibUsbDeviceHandle,
        interfaceNumber: Int,
    ): Int

    fun libusb_bulk_transfer(
        handle: LibUsbDeviceHandle,
        endpoint: Byte,
        data: ByteArray,
        length: Int,
        actualLength: IntByReference,
        timeoutMs: Int,
    ): Int

    fun libusb_get_string_descriptor_ascii(
        handle: LibUsbDeviceHandle,
        descriptorIndex: Byte,
        data: ByteArray,
        length: Int,
    ): Int

    fun libusb_error_name(errorCode: Int): Pointer?
}

internal open class LibUsbPointerType(
    pointer: Pointer? = null,
) : PointerType(pointer)

internal class LibUsbContext(
    pointer: Pointer? = null,
) : LibUsbPointerType(pointer)

internal class LibUsbDevice(
    pointer: Pointer? = null,
) : LibUsbPointerType(pointer)

internal class LibUsbDeviceHandle(
    pointer: Pointer? = null,
) : LibUsbPointerType(pointer)

@Structure.FieldOrder(
    "bLength",
    "bDescriptorType",
    "bcdUSB",
    "bDeviceClass",
    "bDeviceSubClass",
    "bDeviceProtocol",
    "bMaxPacketSize0",
    "idVendor",
    "idProduct",
    "bcdDevice",
    "iManufacturer",
    "iProduct",
    "iSerialNumber",
    "bNumConfigurations",
)
internal class LibUsbDeviceDescriptor : Structure() {
    @JvmField
    var bLength: Byte = 0

    @JvmField
    var bDescriptorType: Byte = 0

    @JvmField
    var bcdUSB: Short = 0

    @JvmField
    var bDeviceClass: Byte = 0

    @JvmField
    var bDeviceSubClass: Byte = 0

    @JvmField
    var bDeviceProtocol: Byte = 0

    @JvmField
    var bMaxPacketSize0: Byte = 0

    @JvmField
    var idVendor: Short = 0

    @JvmField
    var idProduct: Short = 0

    @JvmField
    var bcdDevice: Short = 0

    @JvmField
    var iManufacturer: Byte = 0

    @JvmField
    var iProduct: Byte = 0

    @JvmField
    var iSerialNumber: Byte = 0

    @JvmField
    var bNumConfigurations: Byte = 0
}

@Structure.FieldOrder(
    "bLength",
    "bDescriptorType",
    "wTotalLength",
    "bNumInterfaces",
    "bConfigurationValue",
    "iConfiguration",
    "bmAttributes",
    "maxPower",
    "iface",
    "extra",
    "extra_length",
)
internal class LibUsbConfigDescriptor(
    pointer: Pointer? = null,
) : Structure(pointer) {
    @JvmField
    var bLength: Byte = 0

    @JvmField
    var bDescriptorType: Byte = 0

    @JvmField
    var wTotalLength: Short = 0

    @JvmField
    var bNumInterfaces: Byte = 0

    @JvmField
    var bConfigurationValue: Byte = 0

    @JvmField
    var iConfiguration: Byte = 0

    @JvmField
    var bmAttributes: Byte = 0

    @JvmField
    var maxPower: Byte = 0

    @JvmField
    var iface: Pointer? = null

    @JvmField
    var extra: Pointer? = null

    @JvmField
    var extra_length: Int = 0

    init {
        if (pointer != null) {
            read()
        }
    }
}

@Structure.FieldOrder(
    "altsetting",
    "num_altsetting",
)
internal class LibUsbInterface(
    pointer: Pointer? = null,
) : Structure(pointer) {
    @JvmField
    var altsetting: Pointer? = null

    @JvmField
    var num_altsetting: Int = 0

    init {
        if (pointer != null) {
            read()
        }
    }
}

@Structure.FieldOrder(
    "bLength",
    "bDescriptorType",
    "bInterfaceNumber",
    "bAlternateSetting",
    "bNumEndpoints",
    "bInterfaceClass",
    "bInterfaceSubClass",
    "bInterfaceProtocol",
    "iInterface",
    "endpoint",
    "extra",
    "extra_length",
)
internal class LibUsbInterfaceDescriptor(
    pointer: Pointer? = null,
) : Structure(pointer) {
    @JvmField
    var bLength: Byte = 0

    @JvmField
    var bDescriptorType: Byte = 0

    @JvmField
    var bInterfaceNumber: Byte = 0

    @JvmField
    var bAlternateSetting: Byte = 0

    @JvmField
    var bNumEndpoints: Byte = 0

    @JvmField
    var bInterfaceClass: Byte = 0

    @JvmField
    var bInterfaceSubClass: Byte = 0

    @JvmField
    var bInterfaceProtocol: Byte = 0

    @JvmField
    var iInterface: Byte = 0

    @JvmField
    var endpoint: Pointer? = null

    @JvmField
    var extra: Pointer? = null

    @JvmField
    var extra_length: Int = 0

    init {
        if (pointer != null) {
            read()
        }
    }
}

@Structure.FieldOrder(
    "bLength",
    "bDescriptorType",
    "bEndpointAddress",
    "bmAttributes",
    "wMaxPacketSize",
    "bInterval",
    "bRefresh",
    "bSynchAddress",
    "extra",
    "extra_length",
)
internal class LibUsbEndpointDescriptor(
    pointer: Pointer? = null,
) : Structure(pointer) {
    @JvmField
    var bLength: Byte = 0

    @JvmField
    var bDescriptorType: Byte = 0

    @JvmField
    var bEndpointAddress: Byte = 0

    @JvmField
    var bmAttributes: Byte = 0

    @JvmField
    var wMaxPacketSize: Short = 0

    @JvmField
    var bInterval: Byte = 0

    @JvmField
    var bRefresh: Byte = 0

    @JvmField
    var bSynchAddress: Byte = 0

    @JvmField
    var extra: Pointer? = null

    @JvmField
    var extra_length: Int = 0

    init {
        if (pointer != null) {
            read()
        }
    }
}

internal object LibUsb {
    val native: LibUsbNative by lazy {
        loadNativeLibrary()
    }

    private fun loadNativeLibrary(): LibUsbNative {
        val candidates = listOf(
            "usb-1.0",
            "libusb-1.0",
            "/opt/homebrew/lib/libusb-1.0.dylib",
            "/usr/local/lib/libusb-1.0.dylib",
        )
        var lastError: Throwable? = null
        for (candidate in candidates) {
            try {
                NativeLibrary.getInstance(candidate)
                return Native.load(candidate, LibUsbNative::class.java)
            } catch (throwable: Throwable) {
                lastError = throwable
            }
        }
        throw StLinkException("无法加载 libusb-1.0，本机需要可用的 libusb 运行库", lastError)
    }
}

internal fun Int.toUnsignedByteValue(): Int = this and 0xFF

internal fun Byte.toUnsignedByteValue(): Int = toInt() and 0xFF

internal fun Short.toUnsignedShortValue(): Int = toInt() and 0xFFFF

internal fun Pointer?.isNullPointer(): Boolean = this == null || Pointer.nativeValue(this) == 0L

internal fun LibUsbDeviceDescriptor.vendorId(): Int = idVendor.toUnsignedShortValue()

internal fun LibUsbDeviceDescriptor.productId(): Int = idProduct.toUnsignedShortValue()

internal fun LibUsbDeviceDescriptor.readIndexedAsciiString(
    handle: LibUsbDeviceHandle,
    descriptorIndex: Byte,
): String? {
    if (descriptorIndex.toUnsignedByteValue() == 0) {
        return null
    }
    val buffer = ByteArray(256)
    val size = LibUsb.native.libusb_get_string_descriptor_ascii(handle, descriptorIndex, buffer, buffer.size)
    if (size <= 0) {
        return null
    }
    return buffer.copyOf(size).toString(Charsets.UTF_8).trim().ifBlank { null }
}

internal fun Pointer.pointerAt(index: Int): Pointer? {
    val offset = index.toLong() * Native.POINTER_SIZE
    return getPointer(offset)
}

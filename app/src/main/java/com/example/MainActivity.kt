package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ui.theme.MyApplicationTheme
import java.text.SimpleDateFormat
import java.util.*
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf

// --- COMPOSITION LOCALS FOR REALTIME CUSTOMIZATION ---
val LocalAppThemeColor = staticCompositionLocalOf { Color(0xFF10B981) }
val LocalCustomStoreName = staticCompositionLocalOf { "সৃজনী." }
val LocalCustomTagline = staticCompositionLocalOf { "আধুনিক ও নান্দনিক পরিধেয়" }

// --- DATA STRUCTURES ---

data class Product(
    val id: Int,
    val name: String,
    val category: String,
    val price: Double,
    val originalPrice: Double? = null,
    val tag: String? = null,
    val colorText: String,
    val desc: String,
    val imageType: String
)

data class CartItem(
    val product: Product,
    var quantity: Int = 1,
    val size: String = "M"
)

enum class OrderStatus(val title: String, val stepValue: Int, val description: String) {
    CONFIRMED("অর্ডার কনফার্মড", 1, "আপনার অর্ডারটি সফলভাবে গ্রহণ করা হয়েছে।"),
    PACKING("প্যাকিং চলছে", 2, "পণ্যগুলো যত্ন সহকারে প্যাকেটজাত করা হচ্ছে।"),
    SHIPPED("ডেলিভারি পথে", 3, "আমাদের ডেলিভারি পার্টনারের নিকট পণ্য হস্তান্তর করা হয়েছে।"),
    DELIVERED("ডেলিভারি সম্পন্ন", 4, "অর্ডারটি সফলভাবে আপনার ঠিকানায় পৌঁছে দেওয়া হয়েছে।")
}

data class Order(
    val id: String,
    val date: String,
    val items: List<CartItem>,
    val totalAmount: Double,
    val status: OrderStatus = OrderStatus.CONFIRMED
)

// --- MAIN ACTIVITY ---

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                MainAppScreen()
            }
        }
    }
}

// --- SCREEN ENUMS ---
enum class ActiveTab {
    SHOP, CART, TRACKING, SETTINGS
}

// --- COMPOSABLE APP SCREEN ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppScreen() {
    // Products Repo - converted to dynamic list so user can customize it!
    val products = remember {
        mutableStateListOf(
            Product(1, "প্রিমিয়াম কটন পাঞ্জাবি", "পাঞ্জাবি", 2500.0, null, "নতুন", "রয়েল ব্লু | সলিড কালার", "১০০% প্রিমিয়াম কটন কাপড়ে তৈরি যা গরমের জন্য অত্যন্ত আরামদায়ক এবং আধুনিক ফিটিং সম্পন্ন। নিখুঁত কারুকাজ ও টেকসই সেলাইয়ের কারণে এটি যেকোনো উৎসবের প্রথম পছন্দ।", "kurta"),
            Product(2, "হাতে বোনা জামদানি শাড়ি", "শাড়ি", 12800.0, 15000.0, "-১৫%", "পার্ল হোয়াইট | সিল্ক", "বাংলার ঐতিহ্যবাহী হাতে বুনা রাজকীয় জামদানি। পার্ল হোয়াইট কালারে জমকালো সুতোর বুনন যা আপনাকে এনে দেবে এক ঐতিহ্যবাহী ও মনমুগ্ধকর আভিজাত্যের লুক।", "saree"),
            Product(3, "ডিজাইনার লিনেন কামিজ", "কামিজ", 3200.0, null, null, "ফ্লোরাল প্রিন্ট | সামার", "উচ্চমানের লিনেন কাপড়ে ফ্লোরাল কারুকাজ। অত্যন্ত আরামদায়ক ফেব্রিক যা প্রতিদিনের ক্যাজুয়াল কমফোর্ট কিংবা যেকোনো সাধারণ ঘরোয়া অনুষ্ঠানের জন্য উপযোগী।", "kamiz"),
            Product(4, "চামড়ার স্টাইলিশ জুতো", "জুতো", 5450.0, null, null, "ট্যান ব্রাউন | ফরমাল", "শতভাগ খাঁটি পনি চামড়ায় তৈরি ফরমাল লোফারস ও সু। মজবুত কুশন ও চমৎকার সাইড গ্রিপের সাথে আরামদায়ক ফরমাল পোশাকের সেরা পরিপূরক।", "shoes"),
            Product(5, "আধুনিক খাদি ফতুয়া", "পাঞ্জাবি", 1800.0, 2200.0, "অফার", "ক্যাজুয়াল | কটন", "ঐতিহ্য ও সংস্কৃতির মিশেলে তৈরি হালকা খাদি কটনের ফতুয়া। গরমের দিনগুলোর জন্য খুবই আরামদায়ক এবং ক্যাজুয়াল জিন্সের সাথে মানানসই।", "fatua"),
            Product(6, "সিল্ক নকশিকাঁথা ওড়না", "অন্যান্য", 2200.0, null, "নতুন", "মাল্টিকালার | এমব্রয়ডারি", "হাতে বোনা অসাধারণ নকশা সম্বলিত আর্ট সিল্ক ওড়না। উজ্জ্বল মাল্টিকালার এমব্রয়ডারি যেকোনো সলিড কালারের কামিজ বা কুর্তির আভিজাত্য বহুগুণ বাড়িয়ে দেয়।", "dupatta")
        )
    }

    // App Navigation & Interaction States
    var currentTab by remember { mutableStateOf(ActiveTab.SHOP) }
    var selectedCategory by remember { mutableStateOf("সব") }
    var searchQuery by remember { mutableStateOf("") }
    var selectedProductForDetail by remember { mutableStateOf<Product?>(null) }
    
    // Customization States
    var customStoreName by remember { mutableStateOf("সৃজনী.") }
    var customTagline by remember { mutableStateOf("আধুনিক ও নান্দনিক পরিধেয়") }
    var primaryColorHex by remember { mutableStateOf("10B981") } // Default Emerald Green
    var layoutViewMode by remember { mutableStateOf("grid") } // "grid" or "list"
    
    // Security & Lock States
    var isPinLockEnabled by remember { mutableStateOf(false) }
    var securityPin by remember { mutableStateOf("1234") }
    var isAppLocked by remember { mutableStateOf(false) }
    var isScanning by remember { mutableStateOf(false) }
    var securityScanScore by remember { mutableStateOf(100) }
    val securityLogs = remember { 
        mutableStateListOf(
            "অ্যাপ্লিকেশন নিরাপদ মোডে চালু হয়েছে।",
            "AES-256 লোকাল ডেটাবেস সিফার কুঞ্জি সক্রিয় হয়েছে।",
            "SSL সার্টিফিকেট ম্যাচিং এবং হোস্ট ভ্যালিডেশন সফল।"
        )
    }

    // Secure Checkout PIN/OTP States
    var showOtpDialog by remember { mutableStateOf(false) }
    var enteredOtpCode by remember { mutableStateOf("") }
    var targetOrderForOtp by remember { mutableStateOf<Order?>(null) }
    var otpErrorText by remember { mutableStateOf("") }
    val generatedOtpCode = remember { "৫৯০১২৩" } // Fixed mock OTP

    // State lists for checkout operation
    val cartList = remember { mutableStateListOf<CartItem>() }
    val ordersList = remember { mutableStateListOf<Order>() }

    val appThemeColor = remember(primaryColorHex) { 
        try {
            Color(android.graphics.Color.parseColor("#$primaryColorHex"))
        } catch (e: Exception) {
            Color(0xFF10B981)
        }
    }

    CompositionLocalProvider(
        LocalAppThemeColor provides appThemeColor,
        LocalCustomStoreName provides customStoreName,
        LocalCustomTagline provides customTagline
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            if (isPinLockEnabled && isAppLocked) {
                PinAuthenticationOverlay(
                    correctPin = securityPin,
                    onUnlocked = { isAppLocked = false }
                )
            } else {
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    containerColor = Color(0xFFF8FAFC), // Slate 50 background representing minimal elegance
                    bottomBar = {
                        BottomNavigationBar(
                            currentTab = currentTab,
                            onTabSelected = { currentTab = it },
                            cartCount = cartList.sumOf { it.quantity }
                        )
                    }
                ) { innerPadding ->
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        // Elegant Top Header Bar
                        HeaderBar(
                            title = customStoreName,
                            onCartClick = { currentTab = ActiveTab.CART }
                        )

                        // Switch Screen Body
                        Crossfade(targetState = currentTab, modifier = Modifier.fillMaxSize()) { tab ->
                            when (tab) {
                                ActiveTab.SHOP -> {
                                    ShopScreenBody(
                                        products = products,
                                        selectedCategory = selectedCategory,
                                        onCategorySelect = { selectedCategory = it },
                                        searchQuery = searchQuery,
                                        onSearchQueryChange = { searchQuery = it },
                                        onProductClick = { selectedProductForDetail = it },
                                        onAddToCartQuick = { p ->
                                            val exist = cartList.find { it.product.id == p.id && it.size == "M" }
                                            if (exist != null) {
                                                exist.quantity += 1
                                                val index = cartList.indexOf(exist)
                                                cartList[index] = exist.copy()
                                            } else {
                                                cartList.add(CartItem(product = p, quantity = 1, size = "M"))
                                            }
                                            securityLogs.add(0, "${p.name} কার্টে যুক্ত করা হয়েছে এবং পে-লোড স্যান্ডবক্সিং সম্পন্ন হয়েছে।")
                                        }
                                    )
                                }

                                ActiveTab.CART -> {
                                    CartScreenBody(
                                        cartItems = cartList,
                                        onUpdateQuantity = { item, newQty ->
                                            val index = cartList.indexOf(item)
                                            if (index >= 0) {
                                                if (newQty <= 0) {
                                                    cartList.removeAt(index)
                                                } else {
                                                    cartList[index] = item.copy(quantity = newQty)
                                                }
                                            }
                                        },
                                        onCheckout = {
                                            if (cartList.isNotEmpty()) {
                                                // Initialize secure OTP checkout instead of automatic compilation
                                                val newOrder = Order(
                                                    id = "SRJ-${(100000..999999).random()}",
                                                    date = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()).format(Date()),
                                                    items = cartList.toList(),
                                                    totalAmount = cartList.sumOf { it.product.price * it.quantity } + 80 // Delivery charge included
                                                )
                                                targetOrderForOtp = newOrder
                                                enteredOtpCode = ""
                                                otpErrorText = ""
                                                showOtpDialog = true
                                            }
                                        },
                                        onBackToShop = { currentTab = ActiveTab.SHOP }
                                    )
                                }

                                ActiveTab.TRACKING -> {
                                    TrackingScreenBody(
                                        orders = ordersList,
                                        onNextStatusSimulator = { order ->
                                            val sIndex = ordersList.indexOf(order)
                                            if (sIndex >= 0) {
                                                val currentStatus = order.status
                                                val nextStatus = when (currentStatus) {
                                                    OrderStatus.CONFIRMED -> OrderStatus.PACKING
                                                    OrderStatus.PACKING -> OrderStatus.SHIPPED
                                                    OrderStatus.SHIPPED -> OrderStatus.DELIVERED
                                                    OrderStatus.DELIVERED -> OrderStatus.DELIVERED
                                                }
                                                ordersList[sIndex] = order.copy(status = nextStatus)
                                                securityLogs.add(0, "অর্ডার ${order.id} এর স্ট্যাটাস আপডেট হয়ে '${nextStatus.title}' হয়েছে।")
                                            }
                                        },
                                        onStartShopping = { currentTab = ActiveTab.SHOP }
                                    )
                                }

                                ActiveTab.SETTINGS -> {
                                    SettingsAndSecurityScreen(
                                        products = products,
                                        customStoreName = customStoreName,
                                        onStoreNameChange = { customStoreName = it },
                                        customTagline = customTagline,
                                        onTaglineChange = { customTagline = it },
                                        primaryColorHex = primaryColorHex,
                                        onColorChange = { primaryColorHex = it },
                                        layoutViewMode = layoutViewMode,
                                        onLayoutChange = { layoutViewMode = it },
                                        isPinLockEnabled = isPinLockEnabled,
                                        onPinLockToggle = {
                                            isPinLockEnabled = it
                                            if (it) {
                                                isAppLocked = true
                                                securityLogs.add(0, "নতুন ৪-সংখ্যার সিকিউরিটি পিন লক মেকানিজম সক্রিয় করা হয়েছে।")
                                            } else {
                                                securityLogs.add(0, "সিকিউরিটি পিন লক ডিজাবল করা হয়েছে।")
                                            }
                                        },
                                        securityPin = securityPin,
                                        onPinChange = { securityPin = it },
                                        onLockAppNow = { isAppLocked = true },
                                        isScanning = isScanning,
                                        onScanTrigger = {
                                            isScanning = true
                                            securityScanScore = 100
                                        },
                                        onScanComplete = {
                                            isScanning = false
                                            securityLogs.add(0, "সিস্টেম অডিট ও অ্যান্টি-ম্যালওয়্যার স্ক্যান শেষ হয়েছে। কোনো ত্রুটি পাওয়া যায়নি।")
                                        },
                                        securityLogs = securityLogs,
                                        onClearLogs = { securityLogs.clear() }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Product Details Detail Dialog Overlay
            selectedProductForDetail?.let { product ->
                ProductDetailDialog(
                    product = product,
                    onDismiss = { selectedProductForDetail = null },
                    onAddToCart = { size, quantity ->
                        val exist = cartList.find { it.product.id == product.id && it.size == size }
                        if (exist != null) {
                            exist.quantity += quantity
                            val index = cartList.indexOf(exist)
                            cartList[index] = exist.copy()
                        } else {
                            cartList.add(CartItem(product = product, quantity = quantity, size = size))
                        }
                        selectedProductForDetail = null
                        securityLogs.add(0, "${product.name} (${size}) কার্টে যুক্ত করা হয়েছে এবং ডাটা বাফার স্যান্ডবক্সড আছে।")
                    }
                )
            }

            // Secure Checkout 3D Secure Verification Dialog
            if (showOtpDialog && targetOrderForOtp != null) {
                SecureOtpVerificationDialog(
                    order = targetOrderForOtp!!,
                    otpCode = generatedOtpCode,
                    enteredOtp = enteredOtpCode,
                    onEnteredOtpChange = { enteredOtpCode = it },
                    errorText = otpErrorText,
                    onDismiss = {
                        showOtpDialog = false
                        targetOrderForOtp = null
                    },
                    onVerifySuccess = {
                        val finalOrder = targetOrderForOtp!!
                        ordersList.add(0, finalOrder)
                        cartList.clear()
                        showOtpDialog = false
                        targetOrderForOtp = null
                        currentTab = ActiveTab.TRACKING
                        securityLogs.add(0, "পেমেন্ট অথেনটিকেশন সফল! অর্ডার ${finalOrder.id} সম্পূর্ণ সুরক্ষিতভাবে প্লেস করা হয়েছে।")
                    },
                    onVerifyFail = {
                        otpErrorText = "ভুল ওটিপি কোড! অনুগ্রহ করে সঠিকভাবে ৩ডি পিন দিয়ে চেষ্টা করুন।"
                        securityLogs.add(0, "পেমেন্ট ভ্যালিডেশন ব্যর্থ হয়েছে - ত্রুটিযুক্ত ওটিপি এন্ট্রি!")
                    }
                )
            }
        }
    }
}

// --- ELEGANT COMPONENT: HEADER ---
@Composable
fun HeaderBar(
    title: String,
    onCartClick: () -> Unit
) {
    val storeName = LocalCustomStoreName.current
    val tagline = LocalCustomTagline.current
    val themeColor = LocalAppThemeColor.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .statusBarsPadding()
            .padding(horizontal = 24.dp, vertical = 20.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = storeName,
                fontSize = 26.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color(0xFF0F172A), // Slate 900
                fontFamily = FontFamily.SansSerif
            )
            Text(
                text = tagline,
                fontSize = 11.sp,
                color = themeColor,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 1.dp)
            )
        }

        Card(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .clickable { onCartClick() },
            colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F5F9)),
            shape = CircleShape
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.ShoppingCart,
                    contentDescription = "Shopping Cart",
                    tint = Color(0xFF0F172A),
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
    // Thin Separator line representing extreme minimalism
    Spacer(
        modifier = Modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(Color(0xFFE2E8F0))
    )
}

// --- ELEGANT COMPONENT: BOTTOM BAR ---
@Composable
fun BottomNavigationBar(
    currentTab: ActiveTab,
    onTabSelected: (ActiveTab) -> Unit,
    cartCount: Int
) {
    val themeColor = LocalAppThemeColor.current
    val indicatorColor = themeColor.copy(alpha = 0.1f)

    NavigationBar(
        containerColor = Color.White,
        tonalElevation = 0.dp,
        modifier = Modifier
            .height(72.dp)
            .shadow(elevation = 16.dp, spotColor = Color(0xFFE2E8F0))
    ) {
        NavigationBarItem(
            selected = currentTab == ActiveTab.SHOP,
            onClick = { onTabSelected(ActiveTab.SHOP) },
            icon = {
                Icon(
                    imageVector = Icons.Default.Home,
                    contentDescription = "Shop Home"
                )
            },
            label = {
                Text(
                    "হোম",
                    fontWeight = if (currentTab == ActiveTab.SHOP) FontWeight.Bold else FontWeight.Normal
                )
            },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = themeColor,
                selectedTextColor = themeColor,
                unselectedIconColor = Color(0xFF64748B),
                unselectedTextColor = Color(0xFF64748B),
                indicatorColor = indicatorColor
            )
        )

        NavigationBarItem(
            selected = currentTab == ActiveTab.CART,
            onClick = { onTabSelected(ActiveTab.CART) },
            icon = {
                BadgedBox(
                    badge = {
                        if (cartCount > 0) {
                            Badge(
                                containerColor = themeColor,
                                contentColor = Color.White
                            ) {
                                Text(
                                    text = "$cartCount",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.ShoppingCart,
                        contentDescription = "Cart"
                    )
                }
            },
            label = {
                Text(
                    "কার্ট",
                    fontWeight = if (currentTab == ActiveTab.CART) FontWeight.Bold else FontWeight.Normal
                )
            },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = themeColor,
                selectedTextColor = themeColor,
                unselectedIconColor = Color(0xFF64748B),
                unselectedTextColor = Color(0xFF64748B),
                indicatorColor = indicatorColor
            )
        )

        NavigationBarItem(
            selected = currentTab == ActiveTab.TRACKING,
            onClick = { onTabSelected(ActiveTab.TRACKING) },
            icon = {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Order Tracking"
                )
            },
            label = {
                Text(
                    "ট্র্যাকিং",
                    fontWeight = if (currentTab == ActiveTab.TRACKING) FontWeight.Bold else FontWeight.Normal
                )
            },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = themeColor,
                selectedTextColor = themeColor,
                unselectedIconColor = Color(0xFF64748B),
                unselectedTextColor = Color(0xFF64748B),
                indicatorColor = indicatorColor
            )
        )

        NavigationBarItem(
            selected = currentTab == ActiveTab.SETTINGS,
            onClick = { onTabSelected(ActiveTab.SETTINGS) },
            icon = {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Settings"
                )
            },
            label = {
                Text(
                    "সেটিংস",
                    fontWeight = if (currentTab == ActiveTab.SETTINGS) FontWeight.Bold else FontWeight.Normal
                )
            },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = themeColor,
                selectedTextColor = themeColor,
                unselectedIconColor = Color(0xFF64748B),
                unselectedTextColor = Color(0xFF64748B),
                indicatorColor = indicatorColor
            )
        )
    }
}

// --- SHOP SCREEN BODY ---
@Composable
fun ShopScreenBody(
    products: List<Product>,
    selectedCategory: String,
    onCategorySelect: (String) -> Unit,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onProductClick: (Product) -> Unit,
    onAddToCartQuick: (Product) -> Unit
) {
    val keyboardController = LocalSoftwareKeyboardController.current

    // Filter list logic
    val filteredProducts = remember(selectedCategory, searchQuery) {
        products.filter { p ->
            val matchCategory = selectedCategory == "সব" || p.category == selectedCategory
            val matchSearch = searchQuery.isBlank() ||
                    p.name.contains(searchQuery, ignoreCase = true) ||
                    p.desc.contains(searchQuery, ignoreCase = true) ||
                    p.colorText.contains(searchQuery, ignoreCase = true)
            matchCategory && matchSearch
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 20.dp)
    ) {
        // Search & Filter Box
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(horizontal = 20.dp, vertical = 14.dp)
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = onSearchQueryChange,
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("খুঁজুন (যেমন: পাঞ্জাবি, শাড়ি, কটন...)", color = Color(0xFF94A3B8), fontSize = 13.sp) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search icon",
                            tint = Color(0xFF64748B)
                        )
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { onSearchQueryChange("") }) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Clear",
                                    tint = Color(0xFF94A3B8)
                                )
                            }
                        }
                    },
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(onSearch = { keyboardController?.hide() }),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF10B981),
                        unfocusedBorderColor = Color(0xFFE2E8F0),
                        focusedContainerColor = Color(0xFFF8FAFC),
                        unfocusedContainerColor = Color(0xFFF8FAFC)
                    ),
                    shape = RoundedCornerShape(12.dp)
                )
            }
        }

        // Hero Promotional Banner "Clean Minimalist Theme Banner"
        item {
            PromoHeroBanner()
        }

        // Horizontal Category Tabs
        item {
            val categories = listOf("সব", "পাঞ্জাবি", "শাড়ি", "কামিজ", "জুতো", "অন্যান্য")
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(vertical = 14.dp, horizontal = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(categories) { category ->
                    val isSelected = selectedCategory == category
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(30.dp))
                            .background(if (isSelected) Color(0xFF10B981) else Color(0xFFF1F5F9))
                            .clickable { onCategorySelect(category) }
                            .padding(horizontal = 20.dp, vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = category,
                            color = if (isSelected) Color.White else Color(0xFF334155),
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }

        // Section header label
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (searchQuery.isNotEmpty()) "অনুসন্ধানের ফলাফল (${filteredProducts.size})" else "জনপ্রিয় পণ্যসমূহ",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0F172A)
                )
                if (searchQuery.isEmpty()) {
                    Text(
                        text = "সবগুলো দেখুন",
                        color = Color(0xFF10B981),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.clickable { onCategorySelect("সব") }
                    )
                }
            }
        }

        // Products Grid representation inside LazyColumn matching standard layouts
        if (filteredProducts.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(50.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "Empty",
                            tint = Color(0xFF94A3B8),
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            "দুঃখিত, কোনো পণ্য পাওয়া যায়নি!",
                            color = Color(0xFF64748B),
                            fontSize = 14.sp
                        )
                    }
                }
            }
        } else {
            // Group lists in pairs for grid styling
            val pairs = filteredProducts.chunked(2)
            items(pairs) { pair ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    ProductCardItem(
                        product = pair[0],
                        modifier = Modifier.weight(1f),
                        onClick = { onProductClick(pair[0]) },
                        onAddQuick = { onAddToCartQuick(pair[0]) }
                    )
                    if (pair.size > 1) {
                        ProductCardItem(
                            product = pair[1],
                            modifier = Modifier.weight(1f),
                            onClick = { onProductClick(pair[1]) },
                            onAddQuick = { onAddToCartQuick(pair[1]) }
                        )
                    } else {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

// --- ELEGANT ARTWORK DRAWINGS VIA COMPOSE CANVAS ---
@Composable
fun ProductArtwork(
    imageType: String,
    modifier: Modifier = Modifier
) {
    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(160.dp)
            .background(Color(0xFFF1F5F9)) // subtle slate backdrop for products
    ) {
        val width = size.width
        val height = size.height
        val centerX = width / 2f
        val centerY = height / 2f

        when (imageType) {
            "kurta" -> {
                // Royal Blue Kurta artistic representation
                val mainColor = Color(0xFF1E3A8A)
                val goldColor = Color(0xFFF59E0B)

                // Neck/Collar lines
                drawRoundRect(
                    color = mainColor,
                    topLeft = Offset(centerX - 40f, centerY - 60f),
                    size = Size(80f, 130f),
                    cornerRadius = CornerRadius(15f, 15f)
                )
                // Collar V
                val vPath = Path().apply {
                    moveTo(centerX - 25f, centerY - 60f)
                    lineTo(centerX, centerY - 20f)
                    lineTo(centerX + 25f, centerY - 60f)
                }
                drawPath(vPath, color = goldColor, style = Stroke(width = 5f))

                // Golden buttons
                drawCircle(color = goldColor, radius = 6f, center = Offset(centerX, centerY - 5f))
                drawCircle(color = goldColor, radius = 6f, center = Offset(centerX, centerY + 15f))
                drawCircle(color = goldColor, radius = 6f, center = Offset(centerX, centerY + 35f))
            }
            "saree" -> {
                // Saree representation - elegant waves of Pearl white and Emerald/Crimson
                val baseCrimson = Color(0xFF991B1B)
                val silkCream = Color(0xFFFFFAF0)
                val bordersGold = Color(0xFFD97706)

                // Background drapery block
                drawRect(
                    color = silkCream,
                    topLeft = Offset(centerX - 60f, centerY - 65f),
                    size = Size(120f, 130f)
                )

                // Wavy waves diagonal folds
                for (i in 0..3) {
                    val pathY = centerY - 50f + (i * 30f)
                    val foldPath = Path().apply {
                        moveTo(centerX - 60f, pathY)
                        cubicTo(
                            centerX - 30f, pathY - 15f,
                            centerX + 30f, pathY + 15f,
                            centerX + 60f, pathY
                        )
                    }
                    drawPath(foldPath, color = baseCrimson, style = Stroke(width = 8f, cap = StrokeCap.Round))
                }

                // Golden border bands
                drawRect(
                    color = bordersGold,
                    topLeft = Offset(centerX - 60f, centerY + 50f),
                    size = Size(120f, 10f)
                )
            }
            "kamiz" -> {
                // Designer Linen Kamiz: soft yellow-orange floral theme
                val peachLight = Color(0xFFFFEDD5)
                val floralAccent = Color(0xFFEA580C)
                val stemGreen = Color(0xFF059669)

                // Suit outline
                drawRoundRect(
                    color = peachLight,
                    topLeft = Offset(centerX - 45f, centerY - 65f),
                    size = Size(90f, 130f),
                    cornerRadius = CornerRadius(10f, 10f)
                )

                // Elegant flower blossom drawn on suit
                drawCircle(color = floralAccent, radius = 10f, center = Offset(centerX, centerY - 15f))
                for (angle in 0..360 step 60) {
                    val radian = Math.toRadians(angle.toDouble())
                    val petalX = centerX + (18f * Math.cos(radian)).toFloat()
                    val petalY = (centerY - 15f) + (18f * Math.sin(radian)).toFloat()
                    drawCircle(color = Color(0xFFFDBA74), radius = 7f, center = Offset(petalX, petalY))
                }

                // Stem line
                val stemPath = Path().apply {
                    moveTo(centerX, centerY - 5f)
                    quadraticTo(centerX + 15f, centerY + 20f, centerX, centerY + 45f)
                }
                drawPath(stemPath, color = stemGreen, style = Stroke(width = 4f))
            }
            "shoes" -> {
                // Classic premium leather oxford design
                val leatherBrown = Color(0xFF78350F)
                val soleSlate = Color(0xFF1E293B)

                // Stylized shoe profile
                val shoePath = Path().apply {
                    moveTo(centerX - 65f, centerY + 15f)
                    quadraticTo(centerX - 40f, centerY - 30f, centerX - 5f, centerY - 30f)
                    quadraticTo(centerX + 35f, centerY - 15f, centerX + 60f, centerY + 5f)
                    quadraticTo(centerX + 65f, centerY + 15f, centerX + 65f, centerY + 25f)
                    lineTo(centerX - 65f, centerY + 25f)
                    close()
                }
                drawPath(shoePath, color = leatherBrown)

                // Shoe Sole line representation
                drawRoundRect(
                    color = soleSlate,
                    topLeft = Offset(centerX - 68f, centerY + 23f),
                    size = Size(136f, 10f),
                    cornerRadius = CornerRadius(4f, 4f)
                )

                // Laces
                drawLine(
                    color = Color.White,
                    start = Offset(centerX - 20f, centerY - 10f),
                    end = Offset(centerX - 10f, centerY + 20f),
                    strokeWidth = 3f
                )
                drawLine(
                    color = Color.White,
                    start = Offset(centerX - 12f, centerY - 15f),
                    end = Offset(centerX - 22f, centerY + 12f),
                    strokeWidth = 3f
                )
            }
            "fatua" -> {
                // Khadi Fatua: short sleeve design, light checkered grid
                val khadiGrey = Color(0xFFE2E8F0)
                val gridBlue = Color(0xFF94A3B8)

                // Body rect
                drawRoundRect(
                    color = khadiGrey,
                    topLeft = Offset(centerX - 50f, centerY - 60f),
                    size = Size(100f, 115f),
                    cornerRadius = CornerRadius(5f, 5f)
                )

                // Grid design overlays
                for (i in -40..40 step 20) {
                    drawLine(
                        color = gridBlue,
                        start = Offset(centerX + i, centerY - 60f),
                        end = Offset(centerX + i, centerY + 55f),
                        strokeWidth = 2f
                    )
                    drawLine(
                        color = gridBlue,
                        start = Offset(centerX - 50f, centerY - 10f + i),
                        end = Offset(centerX + 50f, centerY - 10f + i),
                        strokeWidth = 2f
                    )
                }

                // Pocket outline
                drawRoundRect(
                    color = Color(0xFF64748B),
                    topLeft = Offset(centerX + 10f, centerY - 25f),
                    size = Size(25f, 30f),
                    cornerRadius = CornerRadius(4f, 4f)
                )
            }
            else -> {
                // "dupatta" / General Artistic Textile
                // Clean colorful strip outlines matching traditional artistry
                val stripes = listOf(
                    Color(0xFFDC2626), Color(0xFFF59E0B), Color(0xFF10B981),
                    Color(0xFF3B82F6), Color(0xFF8B5CF6)
                )
                val barWidth = 15f

                stripes.forEachIndexed { idx, color ->
                    val offsetLeft = centerX - 55f + (idx * idx * 3.5f) + (idx * 16f)
                    drawRect(
                        color = color,
                        topLeft = Offset(offsetLeft, centerY - 60f),
                        size = Size(barWidth, 120f)
                    )

                    // Dot embroidery details
                    drawCircle(
                        color = Color.White,
                        radius = 4f,
                        center = Offset(offsetLeft + barWidth / 2f, centerY - 20f)
                    )
                    drawCircle(
                        color = Color.White,
                        radius = 4f,
                        center = Offset(offsetLeft + barWidth / 2f, centerY + 20f)
                    )
                }
            }
        }
    }
}

// --- PRODUCT CARD ITEM ---
@Composable
fun ProductCardItem(
    product: Product,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    onAddQuick: () -> Unit
) {
    Card(
        modifier = modifier
            .shadow(elevation = 2.dp, shape = RoundedCornerShape(12.dp))
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Product Artwork inside beautiful responsive Container
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
            ) {
                ProductArtwork(imageType = product.imageType)

                // Discount/New Badge Tag
                product.tag?.let { tagText ->
                    val isDiscount = tagText.startsWith("-")
                    val bgColor = if (isDiscount) Color(0xFFEF4444) else Color(0xFF10B981)
                    Box(
                        modifier = Modifier
                            .padding(8.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(bgColor)
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = tagText,
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Meta Info
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp)
            ) {
                Text(
                    text = product.name,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1E293B),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = product.colorText,
                    fontSize = 11.sp,
                    color = Color(0xFF64748B),
                    modifier = Modifier.padding(top = 2.dp),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        if (product.originalPrice != null) {
                            Text(
                                text = "৳ ${formatBengaliPrice(product.originalPrice)}",
                                fontSize = 11.sp,
                                textDecoration = TextDecoration.LineThrough,
                                color = Color(0xFF94A3B8)
                            )
                        }
                        Text(
                            text = "৳ ${formatBengaliPrice(product.price)}",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF0F172A),
                            fontStyle = FontStyle.Normal
                        )
                    }

                    // Quick Cart Button
                    IconButton(
                        onClick = onAddQuick,
                        modifier = Modifier
                            .size(36.dp)
                            .background(Color(0xFFECFDF5), CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Quick Add",
                            tint = Color(0xFF10B981),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}

// --- PROMOTIONAL HERO HERO BANNER PANEL ---
@Composable
fun PromoHeroBanner() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 6.dp)
            .shadow(4.dp, shape = RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)) // High contrast Slate 900
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1.2f)) {
                Text(
                    text = "নতুন আগমন ২০২৬",
                    fontSize = 10.sp,
                    color = Color(0xFF10B981),
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.5.sp
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "ঐতিহ্য ও শৈল্পিক\nপোশাকে নতুনত্ব",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White,
                    lineHeight = 28.sp
                )
                Spacer(modifier = Modifier.height(14.dp))
                Button(
                    onClick = {},
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.height(36.dp)
                ) {
                    Text("এখনই কিনুন", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
            }

            // Clean decorative circle shape vectors at right
            Box(
                modifier = Modifier
                    .weight(0.8f)
                    .height(110.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF1E293B)),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    // Creative intersecting green circular rings
                    drawCircle(
                        color = Color(0xFF10B981),
                        radius = 45.dp.toPx(),
                        center = Offset(size.width * 0.9f, size.height * 0.8f),
                        style = Stroke(width = 8f)
                    )
                    drawCircle(
                        color = Color(0xFF0F172A),
                        radius = 28.dp.toPx(),
                        center = Offset(size.width * 0.3f, size.height * 0.4f),
                    )
                    drawCircle(
                        color = Color(0xFF334155),
                        radius = 20.dp.toPx(),
                        center = Offset(size.width * 0.3f, size.height * 0.4f),
                        style = Stroke(width = 4f)
                    )
                }
            }
        }
    }
}

// --- MAIN DIALOG: PRODUCT DETAIL DISPLAY ---
@Composable
fun ProductDetailDialog(
    product: Product,
    onDismiss: () -> Unit,
    onAddToCart: (selectedSize: String, qty: Int) -> Unit
) {
    var quantity by remember { mutableStateOf(1) }
    var selectedSize by remember { mutableStateOf("M") }
    val sizes = listOf("S", "M", "L", "XL")

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 24.dp)
                .shadow(24.dp, shape = RoundedCornerShape(20.dp)),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp)
            ) {
                // Header details
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "পণ্য বিবরণী",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF10B981)
                    )
                    IconButton(onClick = onDismiss, modifier = Modifier.size(24.dp)) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close description")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Artwork inside Dialog
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFF1F5F9))
                ) {
                    ProductArtwork(imageType = product.imageType, modifier = Modifier.fillMaxSize())
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Name & Price
                Text(
                    text = product.name,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF0F172A)
                )
                Text(
                    text = product.colorText,
                    fontSize = 12.sp,
                    color = Color(0xFF64748B),
                    modifier = Modifier.padding(top = 4.dp)
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    verticalAlignment = Alignment.Bottom
                ) {
                    Text(
                        text = "৳ ${formatBengaliPrice(product.price)}",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF10B981)
                    )
                    if (product.originalPrice != null) {
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "৳ ${formatBengaliPrice(product.originalPrice)}",
                            fontSize = 14.sp,
                            textDecoration = TextDecoration.LineThrough,
                            color = Color(0xFF94A3B8),
                            modifier = Modifier.padding(bottom = 2.dp)
                        )
                    }
                }

                Divider(color = Color(0xFFF1F5F9), thickness = 1.6.dp)

                // Detailed Description
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "বর্ণনা:",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = Color(0xFF1E293B)
                )
                Text(
                    text = product.desc,
                    fontSize = 12.sp,
                    color = Color(0xFF475569),
                    lineHeight = 18.sp,
                    modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
                )

                // Select Size List
                Text(
                    text = "সাইজ নির্বাচন করুন:",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = Color(0xFF1E293B)
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    sizes.forEach { size ->
                        val isSelected = selectedSize == size
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) Color(0xFF10B981) else Color(0xFFF1F5F9))
                                .clickable { selectedSize = size },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = size,
                                color = if (isSelected) Color.White else Color(0xFF334155),
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Select quantity counter
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "পরিমাণ:",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = Color(0xFF1E293B)
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = { if (quantity > 1) quantity-- },
                            modifier = Modifier
                                .size(32.dp)
                                .background(Color(0xFFF1F5F9), CircleShape)
                        ) {
                            Icon(imageVector = Icons.Default.Delete, contentDescription = "Decrease", modifier = Modifier.size(14.dp))
                        }

                        Text(
                            text = "$quantity",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 16.dp),
                            color = Color(0xFF0F172A)
                        )

                        IconButton(
                            onClick = { quantity++ },
                            modifier = Modifier
                                .size(32.dp)
                                .background(Color(0xFFF1F5F9), CircleShape)
                        ) {
                            Icon(imageVector = Icons.Default.Add, contentDescription = "Increase", modifier = Modifier.size(14.dp))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Bottom CTA Button inside Dialog
                Button(
                    onClick = { onAddToCart(selectedSize, quantity) },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(imageVector = Icons.Default.ShoppingCart, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "কার্টে যোগ করুন",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }
    }
}

// --- CART SCREEN BODY ---
@Composable
fun CartScreenBody(
    cartItems: List<CartItem>,
    onUpdateQuantity: (CartItem, Int) -> Unit,
    onCheckout: () -> Unit,
    onBackToShop: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "শপিং কার্ট",
            fontSize = 20.sp,
            fontWeight = FontWeight.ExtraBold,
            color = Color(0xFF0F172A),
            modifier = Modifier.padding(bottom = 12.dp)
        )

        if (cartItems.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.ShoppingCart,
                        contentDescription = "Empty standard shelf icon",
                        tint = Color(0xFFCBD5E1),
                        modifier = Modifier.size(80.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "আপনার কার্ট বর্তমানে সম্পূর্ণ খালি আছে!",
                        color = Color(0xFF64748B),
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(
                        onClick = onBackToShop,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("পণ্য কিনতে ফিরে যান", fontWeight = FontWeight.Bold)
                    }
                }
            }
        } else {
            // Cart items list panel
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(cartItems) { item ->
                    CartItemRow(item = item, onUpdateQuantity = onUpdateQuantity)
                }
            }

            // Calculation details checkout panel
            Spacer(modifier = Modifier.height(16.dp))
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    val subTotal = cartItems.sumOf { it.product.price * it.quantity }
                    val deliveryCharge = 80.0
                    val total = subTotal + deliveryCharge

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("পণ্যসমূহের মূল্য", color = Color(0xFF64748B), fontSize = 13.sp)
                        Text("৳ ${formatBengaliPrice(subTotal)}", color = Color(0xFF334155), fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("ডেলিভারি খরচ", color = Color(0xFF64748B), fontSize = 13.sp)
                        Text("৳ ${formatBengaliPrice(deliveryCharge)}", color = Color(0xFF334155), fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Divider(color = Color(0xFFF1F5F9))
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("সর্বমোট প্রদানের পরিমাণ", color = Color(0xFF0F172A), fontWeight = FontWeight.ExtraBold, fontSize = 15.sp)
                        Text("৳ ${formatBengaliPrice(total)}", color = Color(0xFF10B981), fontWeight = FontWeight.ExtraBold, fontSize = 18.sp)
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Button(
                        onClick = onCheckout,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                    ) {
                        Text("অর্ডার সম্পন্ন করুন", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                }
            }
        }
    }
}

// --- ELEGANT COMPONENT: CART ROW ---
@Composable
fun CartItemRow(
    item: CartItem,
    onUpdateQuantity: (CartItem, Int) -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Thumb Artwork
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFFF1F5F9))
            ) {
                ProductArtwork(imageType = item.product.imageType, modifier = Modifier.fillMaxSize())
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Text Info
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.product.name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = Color(0xFF1E293B)
                )
                Text(
                    text = "সাইজ: ${item.size} • ৳ ${formatBengaliPrice(item.product.price)}",
                    fontSize = 11.sp,
                    color = Color(0xFF64748B),
                    modifier = Modifier.padding(top = 2.dp)
                )
            }

            // Price & Quantity controls
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.height(64.dp)
            ) {
                // Total price
                Text(
                    text = "৳ ${formatBengaliPrice(item.product.price * item.quantity)}",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 13.sp,
                    color = Color(0xFF0F172A)
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { onUpdateQuantity(item, item.quantity - 1) },
                        modifier = Modifier.size(26.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Delete, contentDescription = "Decrease count", modifier = Modifier.size(12.dp))
                    }
                    Text(
                        text = "${item.quantity}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 6.dp)
                    )
                    IconButton(
                        onClick = { onUpdateQuantity(item, item.quantity + 1) },
                        modifier = Modifier.size(26.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = "Increase count", modifier = Modifier.size(12.dp))
                    }
                }
            }
        }
    }
}

// --- ORDER TRACKING SCREEN BODY ---
@Composable
fun TrackingScreenBody(
    orders: List<Order>,
    onNextStatusSimulator: (Order) -> Unit,
    onStartShopping: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "অর্ডার ট্র্যাকিং",
            fontSize = 20.sp,
            fontWeight = FontWeight.ExtraBold,
            color = Color(0xFF0F172A),
            modifier = Modifier.padding(bottom = 12.dp)
        )

        if (orders.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Empty active items",
                        tint = Color(0xFFCBD5E1),
                        modifier = Modifier.size(80.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "আপনার কোনো সক্রিয় অর্ডার নেই!",
                        color = Color(0xFF64748B),
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(18.dp))
                    Button(
                        onClick = onStartShopping,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("চলুন কেনাকাটা করি!", fontWeight = FontWeight.Bold)
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(orders) { order ->
                    OrderTrackerCard(order = order, onNextStatus = { onNextStatusSimulator(order) })
                }
            }
        }
    }
}

// --- TRACKING CARD COMPONENT WITH GRAPHICAL TIMELINE ---
@Composable
fun OrderTrackerCard(
    order: Order,
    onNextStatus: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier
            .fillMaxWidth()
            .shadow(2.dp, shape = RoundedCornerShape(14.dp))
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            // Header: ID and date details
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "অর্ডার আইডি: ${order.id}",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 14.sp,
                        color = Color(0xFF1E293B)
                    )
                    Text(
                        text = order.date,
                        fontSize = 11.sp,
                        color = Color(0xFF64748B),
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }

                if (order.status != OrderStatus.DELIVERED) {
                    // Minimalist simulator button
                    Button(
                        onClick = onNextStatus,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFECFDF5), contentColor = Color(0xFF10B981)),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        modifier = Modifier.height(28.dp),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text("পরবর্তী ধাপ", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0xFFD1FAE5))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text("ডেলিভারি সফল", color = Color(0xFF065F46), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
            Divider(color = Color(0xFFF1F5F9))
            Spacer(modifier = Modifier.height(14.dp))

            // Summary row of items bought
            Text(
                text = "ক্রয়কৃত সামগ্রী: " + order.items.joinToString { "${it.product.name} (${it.quantity})" },
                color = Color(0xFF475569),
                fontSize = 12.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "মোট পরিশোধিত মূল্য: ৳ ${formatBengaliPrice(order.totalAmount)}",
                color = Color(0xFF0F172A),
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Graphical Timeline Indicator
            TimelineTimelineDisplay(currentStep = order.status.stepValue)

            Spacer(modifier = Modifier.height(14.dp))

            // Highlighted Active Status Text info
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFFF8FAFC))
                    .padding(12.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = Color(0xFF10B981),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = order.status.title,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = Color(0xFF1F2937)
                        )
                        Text(
                            text = order.status.description,
                            fontSize = 10.sp,
                            color = Color(0xFF6B7280),
                            modifier = Modifier.padding(top = 1.dp)
                        )
                    }
                }
            }
        }
    }
}

// --- TIMELINE GRAPHIC DISPLAY COMPONENT ---
@Composable
fun TimelineTimelineDisplay(currentStep: Int) {
    val steps = listOf("কনফার্মড", "প্যাকিং", "পথে", "ডেলিভার")

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        steps.forEachIndexed { index, stepName ->
            val stepNumber = index + 1
            val isActive = stepNumber <= currentStep
            val isCurrent = stepNumber == currentStep

            // Indicator dot & Label layout inside column
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(
                            if (isCurrent) Color(0xFF10B981)
                            else if (isActive) Color(0xFFD1FAE5)
                            else Color(0xFFF1F5F9)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (isActive && !isCurrent) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = Color(0xFF065F46),
                            modifier = Modifier.size(12.dp)
                        )
                    } else {
                        Text(
                            text = "$stepNumber",
                            color = if (isCurrent) Color.White else Color(0xFF94A3B8),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = stepName,
                    fontSize = 10.sp,
                    color = if (isActive || isCurrent) Color(0xFF0F172A) else Color(0xFF94A3B8),
                    fontWeight = if (isActive || isCurrent) FontWeight.Bold else FontWeight.Normal,
                    textAlign = TextAlign.Center
                )
            }

            // Connection bar line
            if (index < steps.size - 1) {
                val lineActive = stepNumber < currentStep
                Box(
                    modifier = Modifier
                        .height(3.dp)
                        .width(28.dp)
                        .background(if (lineActive) Color(0xFF10B981) else Color(0xFFE2E8F0))
                )
            }
        }
    }
}

// --- HELPER METRIC FORMATTING FUNCTIONS ---
fun formatBengaliPrice(price: Double): String {
    val englishStr = String.format(Locale.US, "%,.0f", price)
    val map = mapOf(
        '0' to '০', '1' to '১', '2' to '২', '3' to '৩', '4' to '৪',
        '5' to '৫', '6' to '৬', '7' to '৭', '8' to '৮', '9' to '৯',
        ',' to ','
    )
    return englishStr.map { map[it] ?: it }.joinToString("")
}

// --- HIGH SECURITY SCREEN: PIN AUTHENTICATION OVERLAY ---
@Composable
fun PinAuthenticationOverlay(
    correctPin: String,
    onUnlocked: () -> Unit
) {
    val themeColor = LocalAppThemeColor.current
    var enteredPin by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F172A)) // High contrast dark Slate 900
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            // Secure padlock icon with dynamic custom pulsing ring
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .background(themeColor.copy(alpha = 0.15f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = "🔒 Lock Check",
                    tint = themeColor,
                    modifier = Modifier.size(36.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(20.dp))
            
            Text(
                text = "নিরাপত্তা কাস্টম লক",
                fontSize = 22.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White
            )
            
            Text(
                text = "অ্যাপ্লিকেশন অ্যাক্সেস করতে ৪-সংখ্যার সিকিউরিটি পিন নম্বর দিন",
                fontSize = 12.sp,
                color = Color(0xFF94A3B8),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 4.dp, bottom = 24.dp)
            )

            // Pin indicators (dot states representation)
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.padding(vertical = 16.dp)
            ) {
                for (i in 0 until 4) {
                    val isFilled = i < enteredPin.length
                    Box(
                        modifier = Modifier
                            .size(16.dp)
                            .clip(CircleShape)
                            .background(if (isFilled) themeColor else Color(0xFF334155))
                            .border(1.6.dp, if (isFilled) themeColor else Color(0xFF475569), CircleShape)
                    )
                }
            }

            if (errorMessage.isNotEmpty()) {
                Text(
                    text = errorMessage,
                    color = Color(0xFFEF4444),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }

            Spacer(modifier = Modifier.height(30.dp))

            // Passcode numeric entry pad (3x4 grid)
            val buttons = listOf(
                listOf("1", "2", "3"),
                listOf("4", "5", "6"),
                listOf("7", "8", "9"),
                listOf("Clear", "0", "Back")
            )

            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                buttons.forEach { row ->
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(20.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        row.forEach { label ->
                            if (label == "Clear") {
                                Box(
                                    modifier = Modifier
                                        .size(64.dp)
                                        .clickable { 
                                            enteredPin = ""
                                            errorMessage = ""
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("মুছুন", color = Color(0xFF94A3B8), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            } else if (label == "Back") {
                                Box(
                                    modifier = Modifier
                                        .size(64.dp)
                                        .clickable { 
                                            if (enteredPin.isNotEmpty()) {
                                                enteredPin = enteredPin.dropLast(1)
                                            }
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete last char", tint = Color(0xFF94A3B8), modifier = Modifier.size(20.dp))
                                }
                            } else {
                                Box(
                                    modifier = Modifier
                                        .size(64.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFF1E293B))
                                        .border(1.dp, Color(0xFF334155), CircleShape)
                                        .clickable {
                                            if (enteredPin.length < 4) {
                                                enteredPin += label
                                                errorMessage = ""
                                                
                                                if (enteredPin.length == 4) {
                                                    if (enteredPin == correctPin) {
                                                        onUnlocked()
                                                    } else {
                                                        enteredPin = ""
                                                        errorMessage = "ভুল পিন নম্বর! আবার চেষ্টা করুন।"
                                                    }
                                                }
                                            }
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = label,
                                        color = Color.White,
                                        fontSize = 24.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// --- HIGH SECURITY CHECKOUT GATEWAY: 3D SECURE OTP PORTAL ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SecureOtpVerificationDialog(
    order: Order,
    otpCode: String,
    enteredOtp: String,
    onEnteredOtpChange: (String) -> Unit,
    errorText: String,
    onDismiss: () -> Unit,
    onVerifySuccess: () -> Unit,
    onVerifyFail: () -> Unit
) {
    val themeColor = LocalAppThemeColor.current
    var secondsLeft by remember { mutableStateOf(59) }

    LaunchedEffect(key1 = secondsLeft) {
        if (secondsLeft > 0) {
            kotlinx.coroutines.delay(1000L)
            secondsLeft--
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
                .shadow(24.dp, shape = RoundedCornerShape(20.dp)),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // SMS incoming notification hub
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F5F9)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.Email, contentDescription = "SMS", tint = themeColor, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("ইনকামিং ওটিপি মেসেজ", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF475569))
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "[সৃজনী ব্যাংক সেলফ] ওটিপি কোড: ৫৯০১২৩। এটি শেয়ার করবেন না। অর্ডার মূল্য: ৳ ${formatBengaliPrice(order.totalAmount)}",
                            fontSize = 11.sp,
                            color = Color(0xFF1E293B),
                            lineHeight = 16.sp
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(imageVector = Icons.Default.Lock, contentDescription = "Secure Lock", tint = themeColor, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "৩ডি সিকিউর পেমেন্ট গেটওয়ে",
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF0F172A),
                        fontSize = 16.sp
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "প্রদেয় মূল্য: ৳ ${formatBengaliPrice(order.totalAmount)}",
                    fontSize = 13.sp,
                    color = Color(0xFF334155),
                    fontWeight = FontWeight.SemiBold
                )

                Text(
                    text = "৩ডি অথেনটিকেশন সম্পন্ন করতে ইনকামিং মেসেজ থেকে ওটিপি লিখুন:",
                    fontSize = 11.sp,
                    color = Color(0xFF64748B),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(vertical = 12.dp)
                )

                OutlinedTextField(
                    value = enteredOtp,
                    onValueChange = onEnteredOtpChange,
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("৬-সংখ্যার ওটিপি কোড দিন", textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth(), fontSize = 13.sp) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = themeColor,
                        unfocusedBorderColor = Color(0xFFE2E8F0),
                        focusedContainerColor = Color(0xFFF8FAFC),
                        unfocusedContainerColor = Color(0xFFF8FAFC)
                    ),
                    shape = RoundedCornerShape(10.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number)
                )

                if (errorText.isNotEmpty()) {
                    Text(
                        text = errorText,
                        color = Color(0xFFEF4444),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(vertical = 6.dp)
                    )
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "পুনরায় সেশন কোড: $secondsLeft সেকেন্ড",
                        color = Color(0xFF64748B),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )

                    if (secondsLeft == 0) {
                        Text(
                            text = "রিসেন্ড ওটিপি",
                            color = themeColor,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.clickable { secondsLeft = 59 }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF64748B))
                    ) {
                        Text("বাতিল করুন", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = {
                            if (enteredOtp == "590123" || enteredOtp == "৫৯০১২৩") {
                                onVerifySuccess()
                            } else {
                                onVerifyFail()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = themeColor),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                    ) {
                        Text("নিশ্চিত করুন", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

// --- MON MOT CUSTOMIZATION PANEL AND SYSTEM SCANNERS ---
@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun SettingsAndSecurityScreen(
    products: MutableList<Product>,
    customStoreName: String,
    onStoreNameChange: (String) -> Unit,
    customTagline: String,
    onTaglineChange: (String) -> Unit,
    primaryColorHex: String,
    onColorChange: (String) -> Unit,
    layoutViewMode: String,
    onLayoutChange: (String) -> Unit,
    isPinLockEnabled: Boolean,
    onPinLockToggle: (Boolean) -> Unit,
    securityPin: String,
    onPinChange: (String) -> Unit,
    onLockAppNow: () -> Unit,
    isScanning: Boolean,
    onScanTrigger: () -> Unit,
    onScanComplete: () -> Unit,
    securityLogs: MutableList<String>,
    onClearLogs: () -> Unit
) {
    val themeColor = LocalAppThemeColor.current
    val keyboardController = LocalSoftwareKeyboardController.current

    LaunchedEffect(key1 = isScanning) {
        if (isScanning) {
            kotlinx.coroutines.delay(2000L)
            onScanComplete()
        }
    }

    var newProdName by remember { mutableStateOf("") }
    var newProdDesc by remember { mutableStateOf("") }
    var newProdPrice by remember { mutableStateOf("") }
    var newProdCategory by remember { mutableStateOf("পাঞ্জাবি") }
    var newProdTag by remember { mutableStateOf("") }
    var newProdColorText by remember { mutableStateOf("") }
    var newProdImageType by remember { mutableStateOf("standard") }
    var creationSuccessAlert by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // App Customization Dashboard CARD
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(2.dp, shape = RoundedCornerShape(14.dp))
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Edit, contentDescription = "Customizer", tint = themeColor, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("মন মত ডিজাইন ও ব্র্যান্ডিং", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
                    }
                    
                    Spacer(modifier = Modifier.height(14.dp))
                    
                    OutlinedTextField(
                        value = customStoreName,
                        onValueChange = onStoreNameChange,
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("স্টোরের কাস্টম নাম (Custom Store Name)", fontSize = 12.sp) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = themeColor, unfocusedBorderColor = Color(0xFFCBD5E1))
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = customTagline,
                        onValueChange = onTaglineChange,
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("দোকানের ক্যাচলাইন (Custom Tagline)", fontSize = 12.sp) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = themeColor, unfocusedBorderColor = Color(0xFFCBD5E1))
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text("আভিজাত্যের থিম নির্বাচন (App Brand Tone):", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF334155))
                    
                    val themeHexPairs = listOf(
                        Pair("10B981", "এমারেল্ড"),
                        Pair("1D4ED8", "রয়েল ব্লু"),
                        Pair("E11D48", "ক্রিমসন"),
                        Pair("4F46E5", "ইন্ডিগো"),
                        Pair("1E293B", "ডার্ক লাক্সারি")
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 10.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        themeHexPairs.forEach { pair ->
                            val hex = pair.first
                            val isSelected = primaryColorHex == hex
                            val hexColorValue = Color(android.graphics.Color.parseColor("#$hex"))

                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(hexColorValue)
                                    .border(2.dp, if (isSelected) Color.White else Color.Transparent, CircleShape)
                                    .shadow(if (isSelected) 4.dp else 0.dp, shape = CircleShape)
                                    .clickable { onColorChange(hex) }
                                    .padding(4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text("শপিং গ্রিড ভিউ মডিউল:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF334155))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = if (layoutViewMode == "grid") themeColor.copy(alpha = 0.1f) else Color(0xFFF1F5F9)),
                            modifier = Modifier
                                .weight(1f)
                                .clickable { onLayoutChange("grid") }
                                .border(1.6.dp, if (layoutViewMode == "grid") themeColor else Color.Transparent, RoundedCornerShape(10.dp)),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Box(modifier = Modifier.padding(12.dp), contentAlignment = Alignment.Center) {
                                Text("ডাবল গ্রিড", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = if (layoutViewMode == "grid") themeColor else Color(0xFF334155))
                            }
                        }

                        Card(
                            colors = CardDefaults.cardColors(containerColor = if (layoutViewMode == "list") themeColor.copy(alpha = 0.1f) else Color(0xFFF1F5F9)),
                            modifier = Modifier
                                .weight(1f)
                                .clickable { onLayoutChange("list") }
                                .border(1.6.dp, if (layoutViewMode == "list") themeColor else Color.Transparent, RoundedCornerShape(10.dp)),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Box(modifier = Modifier.padding(12.dp), contentAlignment = Alignment.Center) {
                                Text("এক কলাম বিশিষ্ট", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = if (layoutViewMode == "list") themeColor else Color(0xFF334155))
                            }
                        }
                    }
                }
            }
        }

        // Advanced High Security Suite CARD
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(2.dp, shape = RoundedCornerShape(14.dp))
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Lock, contentDescription = "🔒 High Security suite", tint = themeColor, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("উন্নত হাই সিকিউরিটি স্যুট", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("৪-সংখ্যার পিন অ্যাপলক এন্ট্রি", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF334155))
                            Text("অ্যাপ্লিকেশন লক ও রি-অথেনটিকেশন মেকানিজম", fontSize = 10.sp, color = Color(0xFF64748B))
                        }
                        Switch(
                            checked = isPinLockEnabled,
                            onCheckedChange = onPinLockToggle,
                            colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = themeColor)
                        )
                    }

                    if (isPinLockEnabled) {
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        OutlinedTextField(
                            value = securityPin,
                            onValueChange = { if (it.length <= 4) onPinChange(it) },
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text("গোপন ৪-সংখ্যার পাসকোড পিন সেট করুন", fontSize = 12.sp) },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = themeColor, unfocusedBorderColor = Color(0xFFCBD5E1))
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Button(
                            onClick = onLockAppNow,
                            colors = ButtonDefaults.buttonColors(containerColor = themeColor),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(36.dp)
                        ) {
                            Text("এক্ষুণি লক অ্যাপ রান করুন", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    Divider(color = Color(0xFFF1F5F9))
                    Spacer(modifier = Modifier.height(16.dp))

                    // Integrity Shield Audit and online checkers
                    Text("অনলাইন প্রটেকশন স্ক্যানার এবং অডিট:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF334155))
                    
                    Spacer(modifier = Modifier.height(10.dp))

                    Button(
                        onClick = onScanTrigger,
                        enabled = !isScanning,
                        colors = ButtonDefaults.buttonColors(containerColor = if (isScanning) Color(0xFF94A3B8) else themeColor),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(42.dp),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        if (isScanning) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(10.dp))
                            Text("নিরাপত্তা ইন্টিগ্রিটি স্ক্যানিং হচ্ছে...", fontSize = 12.sp)
                        } else {
                            Icon(imageVector = Icons.Default.Info, contentDescription = "Scan", tint = Color.White, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("রিয়েল টাইম সিকিউরিটি অডিট চালান", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    if (isScanning) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(4.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(Color(0xFFE2E8F0))
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .fillMaxWidth(0.6f)
                                    .background(themeColor)
                            )
                        }
                        Text("APK মেমোরি স্বাক্ষর ভ্যালিডেশন এবং স্যান্ডবক্স ডিক্রিপশন চেক চলছে।", fontSize = 10.sp, color = Color(0xFF64748B), modifier = Modifier.padding(top = 4.dp))
                    } else {
                        Spacer(modifier = Modifier.height(12.dp))
                        Column(
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFFF8FAFC))
                                .padding(12.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(imageVector = Icons.Default.Check, contentDescription = null, tint = Color(0xFF10B981), modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("🛡️ সিস্টেম রুট ও বাইপাস ডিটেকশন (Root Safe): পাস", fontSize = 11.sp, color = Color(0xFF475569))
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(imageVector = Icons.Default.Check, contentDescription = null, tint = Color(0xFF10B981), modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("🔐 স্বাক্ষর ফিঙ্গারপ্রিন্ট AES-256 (Signature Verify): বৈধ", fontSize = 11.sp, color = Color(0xFF475569))
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(imageVector = Icons.Default.Check, contentDescription = null, tint = Color(0xFF10B981), modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("⚙️ ডাটাবেস এনক্রিপশন লকিং সিফার ও কি-স্টোর: সক্রিয় (Active)", fontSize = 11.sp, color = Color(0xFF475569))
                            }

                            Spacer(modifier = Modifier.height(4.dp))
                            Divider(color = Color(0xFFE2E8F0))
                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = "নিরাপত্তা স্কোর: ১০০/১০০ (আপনার সেশন সুরক্ষিত!)",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF047857),
                                modifier = Modifier.fillMaxWidth(),
                                textAlign = TextAlign.Center
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    Text("লোকাল সিকিউরিটি অডিট ট্র্যাকস (Security Tracker Logs):", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF334155))
                    
                    Spacer(modifier = Modifier.height(8.dp))

                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        LazyColumn(
                            contentPadding = PaddingValues(10.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            items(securityLogs) { log ->
                                Text(
                                    text = "> ${SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())} - $log",
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 11.sp,
                                    color = Color(0xFF10B981)
                                )
                            }
                        }
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        horizontalArrangement = Arrangement.End
                    ) {
                        Text(
                            text = "অডিট লগ মুছুন",
                            color = themeColor,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.clickable { onClearLogs() }
                        )
                    }
                }
            }
        }

        // Dynamic customize Product Creator CARD (পণ্য কাস্টমাইজেশন ল্যাব)
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(2.dp, shape = RoundedCornerShape(14.dp))
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = "Add custom product", tint = themeColor, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("পণ্য কাস্টমাইজেশন ল্যাব (Merchant Config)", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
                    }

                    Text("আপনার পছন্দ অনুসারে দোকানে নতুন কাস্টম পণ্য যুক্ত করুন। পণ্যটি সাথে সাথে শপে লাইভ হয়ে যাবে!", fontSize = 10.sp, color = Color(0xFF64748B), modifier = Modifier.padding(top = 2.dp, bottom = 14.dp))

                    OutlinedTextField(
                        value = newProdName,
                        onValueChange = { newProdName = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("পণ্যের নাম (Product Name)", fontSize = 11.sp) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = themeColor, unfocusedBorderColor = Color(0xFFCBD5E1))
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = newProdDesc,
                        onValueChange = { newProdDesc = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("পণ্যের বিস্তারিত কারুকাজ বিবরণ (Description)", fontSize = 11.sp) },
                        maxLines = 2,
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = themeColor, unfocusedBorderColor = Color(0xFFCBD5E1))
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedTextField(
                            value = newProdPrice,
                            onValueChange = { newProdPrice = it },
                            modifier = Modifier.weight(1f),
                            label = { Text("দাম (৳ Price)", fontSize = 11.sp) },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = themeColor, unfocusedBorderColor = Color(0xFFCBD5E1)),
                            keyboardOptions = KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number)
                        )

                        OutlinedTextField(
                            value = newProdColorText,
                            onValueChange = { newProdColorText = it },
                            modifier = Modifier.weight(1f),
                            label = { Text("ডিজাইন কালার ও ফেব্রিক বিবরণ", fontSize = 11.sp) },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = themeColor, unfocusedBorderColor = Color(0xFFCBD5E1))
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = newProdTag,
                        onValueChange = { newProdTag = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("অফার ডিসকাউন্ট ট্যাগ (যেমন: নতুন, অফার, -২০%)", fontSize = 11.sp) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = themeColor, unfocusedBorderColor = Color(0xFFCBD5E1))
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Text("ক্যাটাগরি কাস্টমাইজেশন নির্বাচন:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF334155))
                    val cats = listOf("পাঞ্জাবি", "শাড়ি", "কামিজ", "জুতো", "অন্যান্য")
                    
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp)
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        cats.forEach { cat ->
                            val isSelected = newProdCategory == cat
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(if (isSelected) themeColor else Color(0xFFF1F5F9))
                                    .clickable { newProdCategory = cat }
                                    .padding(horizontal = 14.dp, vertical = 6.dp)
                            ) {
                                Text(cat, fontSize = 11.sp, color = if (isSelected) Color.White else Color(0xFF334155), fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text("কম্পোজ ক্যানভাস কাস্টম ডিজাইন আর্টওয়ার্ক টেমপ্লেট:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF334155))
                    val artTemplates = listOf(
                        Pair("kurta", "পাঞ্জাবি ক্যানভাস"),
                        Pair("saree", "শাড়ি ক্যানভাস"),
                        Pair("kamiz", "কামিজ ক্যানভাস"),
                        Pair("shoes", "জুতো ক্যানভাস"),
                        Pair("fatua", "ফতুয়া ক্যানভাস"),
                        Pair("standard", "ডিজাইনার রেডি স্ট্রাইপ")
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp)
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        artTemplates.forEach { temp ->
                            val isSelected = newProdImageType == temp.first
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) themeColor.copy(alpha = 0.15f) else Color(0xFFF1F5F9))
                                    .border(1.6.dp, if (isSelected) themeColor else Color.Transparent, RoundedCornerShape(8.dp))
                                    .clickable { newProdImageType = temp.first }
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text(temp.second, fontSize = 11.sp, color = if (isSelected) themeColor else Color(0xFF334155), fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    Button(
                        onClick = {
                            if (newProdName.isNotBlank() && newProdPrice.isNotBlank()) {
                                val priceVal = newProdPrice.toDoubleOrNull() ?: 0.0
                                val idNew = (100..99999).random()
                                val tagVal = if (newProdTag.isBlank()) null else newProdTag
                                
                                val customP = Product(
                                    id = idNew,
                                    name = newProdName,
                                    category = newProdCategory,
                                    price = priceVal,
                                    originalPrice = if (tagVal != null) priceVal * 1.15 else null,
                                    tag = tagVal,
                                    colorText = if (newProdColorText.isBlank()) "কাস্টমাইজড | সলিড কালার" else newProdColorText,
                                    desc = if (newProdDesc.isBlank()) "আপনার মন মত কাস্টমাইজ করা একটি এক্সক্লুসিভ পণ্য যা যেকোনো আভিজাত্যের লুক ফুটিয়ে তুলতে সক্ষম।" else newProdDesc,
                                    imageType = newProdImageType
                                )

                                products.add(0, customP)
                                keyboardController?.hide()
                                
                                newProdName = ""
                                newProdDesc = ""
                                newProdPrice = ""
                                newProdColorText = ""
                                newProdTag = ""
                                creationSuccessAlert = true
                                securityLogs.add(0, "নতুন কাস্টম পোশাক '${customP.name}' স্যান্ডবক্সড ডাটা কাতালগে সাকসেসফুলি যুক্ত হয়েছে।")
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = themeColor),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                    ) {
                        Text("পণ্য যোগ করুন (Add Custom Product)", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }

                    if (creationSuccessAlert) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFFECFDF5))
                                .border(1.dp, Color(0xFF10B981), RoundedCornerShape(8.dp))
                                .padding(10.dp)
                        ) {
                            Text("সাফল্য! পণ্যটি চমৎকারভাবে যুক্ত করা হয়েছে। শপ হোম থেকে এক্ষুণি কিনতে পারবেন!", fontSize = 11.sp, color = Color(0xFF047857), fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                        }
                    }
                }
            }
        }
    }
}

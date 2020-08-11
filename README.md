# Gallery
一个相册应用，采用组件化编程
读取图片的方法
该图库采用的是ContenResolver(内容提供器) 来读取手机中的图片，contentResolver处理可以读取媒体库之外还可以读取短信联系人等信息。

ContenResolver可以通过一个context对象来获取
```java
ContentResolver contentResolver = mContext.getContentResolver();
```

ContenResolver 读取图片的方法类似于数据库，所以也可以将ContenResolver理解为Android系统中存在的一个用于给应用获取手机数据的一个数据库；获取数据的方法和Android操作数据库相似，可以通过query()方法获取一个Cursor类来获取数据
```java
Cursor cursor = contentResolver.query( Uri uri, String[] projection, 
String selection,String[] selectionArgs, String sortOrder) 
```

下面分别对各个参数，进行解释：

参数 描述
uri 指定查询某一张表
projection 指定查询的列名
selection 指定对应的约束条件
selectionArgs 为占位符提供对应的值
sortOrder 指定查询结果的排序方式

下面是代码中传入的具体参数：
```java
@Override
protected Uri getScanUri() {
    return MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
}

@Override
protected String[] getProjection() {
    return new String[]{
            MediaStore.Images.Media.DATA,            //路径
            MediaStore.Images.Media.MIME_TYPE,       //媒体类型
            MediaStore.Images.Media.BUCKET_ID,       //所在文件夹ID
            MediaStore.Images.Media.BUCKET_DISPLAY_NAME,  //所在文件夹名
            MediaStore.Images.Media.DATE_TAKEN            //图片的创建时间
    };
}

@Override
protected String getSelection() {
    return MediaStore.Images.Media.MIME_TYPE + "=? or " + MediaStore.Images.Media.MIME_TYPE + "=?" + " or " + MediaStore.Images.Media.MIME_TYPE + "=?";
}

@Override
protected String[] getSelectionArgs() {
    return new String[]{"image/jpeg", "image/png", "image/gif"};
}
```


同时本次课题由于考虑到读取图片需要的时间，以及Android系统是不建议在主线程中有耗时操作的，所以采用异步的方式读取图片数据——主线程中展示图片，子线程中读取数据，所以这里采用MVP模式进行异步操作:
M（model）负责数据的请求，解析，过滤等数据操作
V（View）:负责UI的展示，和事件的监听等
P（presenter）为业务处理层，既能调用UI逻辑，又能请求数据，该层为纯Java类，不涉及任何Android API。
三层之间的调用顺序为view->presenter->model，注意不能反向调用，不能跨级调用：


上图为它们之间的调用关系。
MVP的优缺点
优点： 单一职责， Model， View， Presenter只处理单一逻辑

解耦：Model层的修改和View层的修改互不影响

面向接口编程，依赖抽象：Presenter和View互相持有抽象引用，对外隐藏内部实现细节。

可能存在的问题：

Model进行一步操作的时候，获取结果通过Presenter会传到View的时候，出现View引用的空指针异常。
Presenter和View相互持有引用，解除不及时的话容易出现内存泄漏。

但以上的问题可以通过代码逻辑避免掉的，而带来的好处也是巨大的。

三、加载图片的方法和对图片的缓存
采用Glide加载图片的流程如下图，从中也可以看出Glide的缓存机制

1.ActiveResources 是一种弱引用缓存，主要用于缓存正在使用的数据；Glide中有一个EngineResource类用来记录图片被使用过的次数，里面的acquired变量用来记录图片被引用的次数，调用acquire()方法会让变量加1，调用release()方法会让变量减1，当变量变为0的时候会将弱引用的缓存放到MemoryCache中，并且从弱引用缓存中删除掉该缓存。使用的时候如果弱引用缓存中不存在会从MemoryCache中找，找到的话会将它取出来，存在这里，并且从MemoryCache中删除

2.MemoryCache是一种采用LruCache算法的缓存方式，也叫近期最少使用算法。它的主要算法原理就是把最近使用的对象用强引用存储在LinkedHashMap中，并且把最近最少使用的找，对象在缓存值达到预设定值之前从内存中移除。

3.磁盘缓存：
DiskCacheStrategy.NONE： 表示不缓存任何内容。
DiskCacheStrategy.RESOURCE： 在资源解码后将数据写入磁盘缓存，即经过缩放等转换后的图片资源。
DiskCacheStrategy.DATA： 在资源解码前将原始数据写入磁盘缓存。
DiskCacheStrategy.ALL ： 使用DATA和RESOURCE缓存远程数据，仅使用RESOURCE来缓存本地数据。
DiskCacheStrategy.AUTOMATIC：它会尝试对本地和远程图片使用最佳的策略。当你加载远程数据（比如，从URL下载）时，AUTOMATIC 策略仅会存储未被你的加载过程修改过(比如，变换，裁剪–译者注)的原始数据，因为下载远程数据相比调整磁盘上已经存在的数据要昂贵得多。对于本地数据，AUTOMATIC 策略则会仅存储变换过的缩略图，因为即使你需要再次生成另一个尺寸或类型的图片，取回原始数据也很容易。默认使用这种缓存策略。

四、UI实现：
1.图片卡片样式展示
使用RecyclerView 结合GridLayoutManager 以及自定义一个正方形ImageView进行卡片式的图片展示。

2.图片长按移动和滑动删除：
使用 ItemTouchHelper.Callback 这个RecyclerView开放出来的接口，对Item 的Drag事件和Swipe事件进行监听，在getMovementFlags 这个方法中进行操作行为的监听：
```java
final int dragFlags = ItemTouchHelper.UP | ItemTouchHelper.DOWN | ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT;
final int swipeFlags = ItemTouchHelper.START;
return makeMovementFlags(dragFlags, swipeFlags);
```


其中dragFlags代表长按移动的行为，swipeFlags代表滑动删除的行为。
之后再重写下面两个方法对长按移动和滑动删除进行回调：
```java
@Override
public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder source, RecyclerView.ViewHolder target) {
    //长按移动的回调
    if (source.getItemViewType() != target.getItemViewType()) {
        return false;
    }
    if (isFirstUnable) {
        if (source.getAdapterPosition() == 0 || target.getAdapterPosition() == 0) {
            return false;
        }
    }
    mAdapter.onItemMove(source.getAdapterPosition(), target.getAdapterPosition());
    return true;
}

@Override
public void onSwiped(RecyclerView.ViewHolder viewHolder, int i) {
    // 滑动删除的回调
    mAdapter.onItemDismiss(viewHolder.getAdapterPosition());
}
```

为了有更好的页面展示效果，可以重写下面三个方法对背景和透明度进行更改
```java
@Override
public void onChildDraw(Canvas c, RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
    //在View绘制时执行的方法，可以改写透明度，写出图片渐渐消失的效果
    if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {
        final float alpha = ALPHA_FULL - Math.abs(dX) / (float) viewHolder.itemView.getWidth();
        viewHolder.itemView.setAlpha(alpha);
        viewHolder.itemView.setTranslationX(dX);
    } else {
        super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
    }
}

@Override
public void onSelectedChanged(RecyclerView.ViewHolder viewHolder, int actionState) {
    // 选中对应图片时执行的方法
    if (actionState != ItemTouchHelper.ACTION_STATE_IDLE) {
        if (viewHolder instanceof ItemTouchHelperViewHolder) {
            // Let the view holder know that this item is being moved or dragged
            ItemTouchHelperViewHolder itemViewHolder = (ItemTouchHelperViewHolder) viewHolder;
            itemViewHolder.onItemSelected();
        }
    }

    super.onSelectedChanged(viewHolder, actionState);
}

@Override
public void clearView(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
    super.clearView(recyclerView, viewHolder);
   //取消用户行为时执行的方法
    viewHolder.itemView.setAlpha(ALPHA_FULL);

    if (viewHolder instanceof ItemTouchHelperViewHolder) {
        // Tell the view holder it's time to restore the idle state
        ItemTouchHelperViewHolder itemViewHolder = (ItemTouchHelperViewHolder) viewHolder;
        itemViewHolder.onItemClear();
    }
}
```
3.底部弹窗选择读取不同文件夹中的图片

通过自定义的PopupWindow结合RecyclerView实现，并向外提供获取数据的接口。

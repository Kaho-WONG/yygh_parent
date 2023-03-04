import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;

/**
 * @description: 测试是否能够使用 SDK 操作自己的 oss 服务
 * @author: Kaho
 * @create: 2023-03-04 11:46
 **/
public class OssTest {
    public static void main(String[] args) {
        // oss bucket 的 Endpoint 地域节点
        String endpoint = "https://oss-cn-shenzhen.aliyuncs.com";
        // 阿里云主账号 Accessey 拥有所有API的访问权限
        String accessKeyId = "自己的accesskey";
        String accessKeySecret = "自己的accesskey";
        String bucketName = "yygh-kaho-test";

        // 创建OSSClient实例
        OSS ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);

        // 创建存储空间
        ossClient.createBucket(bucketName);

        // 关闭OSSClient
        ossClient.shutdown();
    }
}

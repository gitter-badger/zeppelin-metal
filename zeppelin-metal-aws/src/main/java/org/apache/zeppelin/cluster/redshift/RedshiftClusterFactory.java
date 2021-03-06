package org.apache.zeppelin.cluster.redshift;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.services.redshift.AmazonRedshiftClient;
import com.amazonaws.services.redshift.model.Cluster;
import com.amazonaws.services.redshift.model.CreateClusterRequest;
import com.amazonaws.services.redshift.model.DeleteClusterRequest;
import com.amazonaws.services.redshift.model.DescribeClustersRequest;
import com.amazonaws.services.redshift.model.DescribeClustersResult;

/**
 * Interpreter Rest API
 *
 */
public class RedshiftClusterFactory {
  static Logger logger = LoggerFactory.getLogger(RedshiftClusterFactory.class);
  
  public static AmazonRedshiftClient client = new AmazonRedshiftClient(
      new DefaultAWSCredentialsProviderChain());
  
  public RedshiftClusterFactory() {}

  public ClusterSettingRedshift createCluster(String name, String instanceType, 
      int slaves, String user, String passw) {
    ClusterSettingRedshift clustSetting = new ClusterSettingRedshift(name, slaves,
        "starting", null, "", "redshift", instanceType, null);
    createClusterRedshift(name, slaves, user, passw, instanceType);
    
    return clustSetting;
  }
  
  public void createClusterRedshift(String name, int slaves, 
      String user, String passw, String type) {
    CreateClusterRequest request = new CreateClusterRequest()
        .withClusterIdentifier(name)
        .withMasterUsername(user)
        .withMasterUserPassword(passw)
        .withNodeType(type)
        .withNumberOfNodes(slaves);          
        
    Cluster createResponse = client.createCluster(request);
    logger.info("Created cluster " + createResponse.getClusterIdentifier());
  }

  public String getStatus(String clusterId) {
    String status = null;
    String state = null;
    DescribeClustersResult result = client.describeClusters(new DescribeClustersRequest()
        .withClusterIdentifier(clusterId));
    List<Cluster> Redshiftclusters = result.getClusters();
    for (Cluster cluster: Redshiftclusters) {
      if (cluster.getClusterIdentifier().equals(clusterId)) {
        state = cluster.getClusterStatus();
      }
    }
    
    switch (state) {
        case "available":
          status = "running";
          break;
        case "creating":
          status = "starting";
          break;
        case "deleting":
          status = "deleting";
          break;
        default:
          status = "failed";
          break;
    }
    
    return status;
  }
  
  public String getUrlRedshift(String clusterId) {
    DescribeClustersResult result = client.describeClusters(new DescribeClustersRequest()
        .withClusterIdentifier(clusterId));
    String url = result.getClusters().get(0).getEndpoint().getAddress();
    return url;
  }
  
  public void remove(String clusterId) {
    removeRedshiftCluster(clusterId);
  }
  
  public void removeRedshiftCluster(String clusterId) {
    client.deleteCluster(new DeleteClusterRequest()
        .withClusterIdentifier(clusterId)
        .withSkipFinalClusterSnapshot(true));
  }
}

package com.aldunelabs.maven.codeartifact;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.auth.BasicSessionCredentials;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.services.codeartifact.AWSCodeArtifact;
import com.amazonaws.services.codeartifact.AWSCodeArtifactClientBuilder;
import com.amazonaws.services.codeartifact.model.AWSCodeArtifactException;
import com.amazonaws.services.codeartifact.model.DeletePackageVersionsRequest;
import com.amazonaws.services.codeartifact.model.DescribePackageVersionRequest;
import com.amazonaws.services.codeartifact.model.PackageFormat;
import com.amazonaws.services.codeartifact.model.ResourceNotFoundException;

@Mojo(name = "delete-codeartifact", defaultPhase = LifecyclePhase.DEPLOY)
public class MavenAwsDeleteArtifact extends AbstractMojo {

	@Parameter(defaultValue = "${project}", required = true, readonly = true)
	MavenProject project;

	@Parameter(defaultValue = "eu-east-1", name = "region", required = true, property = "CODEARTIFACT_REGION")
	String region;

	@Parameter(required = true, name = "repository", property = "CODEARTIFACT_REPOSITORY")
	String repository;

	@Parameter(required = true, name = "owner", property = "CODEARTIFACT_OWNER")
	String owner;

	@Parameter(required = true, name = "domain", property = "CODEARTIFACT_DOMAIN")
	String domain;

	@Parameter(name = "profile", property = "AWS_PROFILE")
	String profile;

	@Parameter(defaultValue = "", name = "accesskey", property = "AWS_ACCESSKEY")
	String accesskey;

	@Parameter(defaultValue = "", name = "secretkey", property = "AWS_SECRETKEY")
	String secretkey;

	@Parameter(defaultValue = "", name = "token", property = "AWS_SESSIONTOKEN")
	String token;

	private static boolean isNullOrEmpty(String str) {
		if (str == null)
			return true;
		return str.isEmpty();
	}

	private AWSCredentialsProvider buildCredentialsProvider() {

		if (isNullOrEmpty(profile) == false) {
			return new ProfileCredentialsProvider(profile);
		} else if (isNullOrEmpty(accesskey) == false && isNullOrEmpty(secretkey) == false
				&& isNullOrEmpty(token) == false) {
			return new AWSStaticCredentialsProvider(new BasicSessionCredentials(accesskey, secretkey, token));
		} else if (isNullOrEmpty(accesskey) == false && isNullOrEmpty(secretkey) == false) {
			return new AWSStaticCredentialsProvider(new BasicAWSCredentials(accesskey, secretkey));
		}

		return null;
	}

	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {
		dumpProperties();

		AWSCodeArtifactClientBuilder builder = AWSCodeArtifactClientBuilder.standard().withRegion(region)
				.withCredentials(buildCredentialsProvider());
		AWSCodeArtifact codeartifact = builder.build();

		try {
			if (isPackageVersionPresent(codeartifact)) {
				// @formatter:off
				codeartifact.deletePackageVersions(new DeletePackageVersionsRequest().withDomain(this.domain)
						.withDomainOwner(owner)
						.withFormat(PackageFormat.Maven)
						.withRepository(repository)
						.withNamespace(project.getGroupId())
						.withPackage(project.getArtifactId())
						.withVersions(project.getVersion()));
				// @formatter:on
			} else {
				getLog().info(String.format("The artifact %s:%s:%s, doesn't exist", project.getGroupId(),
						project.getArtifactId(), project.getVersion()));
			}
		} catch (ResourceNotFoundException e) {
			// Silent
			getLog().info(String.format("The artifact %s:%s:%s, doesn't exist", project.getGroupId(),
					project.getArtifactId(), project.getVersion()));

			return;
		} catch (AWSCodeArtifactException e) {
			getLog().error(String.format("Unable to delete the artifact %s:%s:%s", project.getGroupId(),
					project.getArtifactId(), project.getVersion()), e);

			throw new MojoFailureException(e, "Delete package", e.getErrorMessage());
		}

		getLog().info(String.format("The artifact %s:%s:%s is deleted", project.getGroupId(), project.getArtifactId(),
				project.getVersion()));
	}

	private boolean isPackageVersionPresent(AWSCodeArtifact codeartifact) {
		// @formatter:off
		try {
			codeartifact.describePackageVersion(new DescribePackageVersionRequest().withDomain(domain).withFormat(PackageFormat.Maven)
						.withRepository(repository)
						.withNamespace(project.getGroupId())
						.withPackage(project.getArtifactId())
						.withPackageVersion(project.getVersion()));
		} catch (ResourceNotFoundException e) {
			return false;
		}
		// @formatter:on
		
		return true;
	}

	private void dumpProperty(String name, String value) {
		if (isNullOrEmpty(value))
			getLog().info(String.format("%s not defined", name));
		else
			getLog().info(String.format("%s=%s", name, value));
	}

	private void dumpProperties() {
		dumpProperty("region", region);
		dumpProperty("repository", repository);
		dumpProperty("owner", owner);
		dumpProperty("domain", domain);
		dumpProperty("profile", profile);
		dumpProperty("accesskey", accesskey);
		dumpProperty("secretkey", secretkey);
		dumpProperty("token", token);
	}
}

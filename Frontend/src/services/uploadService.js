import axios from 'axios';
import apiClient from './apiClient';

export async function getPresignedUploadUrl(file) {
  const presignedResponse = await apiClient.post('/s3/presigned-url', {
    fileName: file.name,
    size: file.size,
    contentType: file.type,
  });

  return presignedResponse.data;
}

export async function uploadFileToS3(uploadUrl, file) {
  await axios.put(uploadUrl, file, {
    headers: {
      'Content-Type': file.type,
    },
  });
}

export async function uploadEventImage(file) {
  const { uploadUrl, key } = await getPresignedUploadUrl(file);
  await uploadFileToS3(uploadUrl, file);

  return key;
}

export async function getImageViewUrl(key) {
  const response = await apiClient.get('/s3/view-url', {
    params: { key },
  });

  return response.data.uploadUrl;
}
